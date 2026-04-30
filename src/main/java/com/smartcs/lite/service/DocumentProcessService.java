package com.smartcs.lite.service;

import com.smartcs.lite.model.entity.DocumentEntity;
import com.smartcs.lite.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final VectorStore vectorStore;

    /**
     * 异步处理文档：下载 → 解析 → 分块 → 向量化
     */
    @Async("knowledgeTaskExecutor")
    public void processDocument(Long docId, Long tenantId, Long kbId) {
        DocumentEntity doc = documentRepository.findById(docId).orElse(null);
        if (doc == null) return;

        try {
            doc.setStatus("PROCESSING");
            documentRepository.save(doc);

            // 1. 从 RustFS 下载文件
            InputStream inputStream = storageService.download(doc.getFileKey());

            // 2. 使用 Apache Tika 解析文档内容
            Tika tika = new Tika();
            String textContent = tika.parseToString(inputStream);

            if (textContent.isBlank()) {
                doc.setStatus("FAILED");
                doc.setErrorMessage("文档内容为空");
                documentRepository.save(doc);
                return;
            }

            // 3. 文本分块
            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    .withChunkSize(512)
                    .withMinChunkSizeChars(100)
                    .withMinChunkLengthToEmbed(50)
                    .build();

            Document aiDoc = new Document(textContent, Map.of(
                    "tenant_id", String.valueOf(tenantId),
                    "kb_id", String.valueOf(kbId),
                    "doc_id", String.valueOf(docId),
                    "title", doc.getTitle(),
                    "type", "document"
            ));

            List<Document> chunks = splitter.apply(List.of(aiDoc));

            // 4. 向量化并存储到 PGvector
            vectorStore.add(chunks);

            // 5. 更新文档状态
            doc.setStatus("READY");
            doc.setChunkCount(chunks.size());
            documentRepository.save(doc);

            log.info("文档处理完成: docId={}, chunks={}", docId, chunks.size());

        } catch (Exception e) {
            log.error("文档处理失败: docId={}", docId, e);
            doc.setStatus("FAILED");
            doc.setErrorMessage(e.getMessage());
            documentRepository.save(doc);
        }
    }
}
