package com.smartcs.lite.service;

import com.smartcs.lite.model.entity.DocumentEntity;
import com.smartcs.lite.model.entity.Faq;
import com.smartcs.lite.model.entity.KnowledgeBase;
import com.smartcs.lite.repository.DocumentRepository;
import com.smartcs.lite.repository.FaqRepository;
import com.smartcs.lite.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeBaseRepository kbRepository;
    private final FaqRepository faqRepository;
    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final DocumentProcessService documentProcessService;
    private final VectorStore vectorStore;

    // ==================== 知识库管理 ====================

    public List<KnowledgeBase> listKnowledgeBases(Long tenantId) {
        return kbRepository.findByTenantId(tenantId);
    }

    public KnowledgeBase createKnowledgeBase(Long tenantId, String name, String description) {
        return kbRepository.save(KnowledgeBase.builder()
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .build());
    }

    // ==================== FAQ 管理 ====================

    public List<Faq> listFaqs(Long kbId, Long tenantId) {
        return faqRepository.findByKbIdAndTenantId(kbId, tenantId);
    }

    /**
     * 添加 FAQ 并自动向量化
     */
    @Async("knowledgeTaskExecutor")
    public void addFaq(Long kbId, Long tenantId, String question, String answer, String category) {
        // 1. 保存到数据库
        Faq faq = faqRepository.save(Faq.builder()
                .kbId(kbId)
                .tenantId(tenantId)
                .question(question)
                .answer(answer)
                .category(category)
                .build());

        // 2. 向量化存储（将问题+答案一起作为文档存储）
        String content = "问题: " + question + "\n答案: " + answer;
        Document doc = new Document(
                content,
                Map.of(
                        "tenant_id", String.valueOf(tenantId),
                        "kb_id", String.valueOf(kbId),
                        "faq_id", String.valueOf(faq.getId()),
                        "type", "faq"
                )
        );
        vectorStore.add(List.of(doc));

        log.info("FAQ 已添加并向量化: id={}, question={}", faq.getId(), question);
    }

    public void deleteFaq(Long faqId) {
        faqRepository.deleteById(faqId);
    }

    // ==================== 文档管理 ====================

    public List<DocumentEntity> listDocuments(Long kbId, Long tenantId) {
        return documentRepository.findByKbIdAndTenantId(kbId, tenantId);
    }

    /**
     * 上传文档并异步处理
     */
    public DocumentEntity uploadDocument(Long kbId, Long tenantId,
                                         MultipartFile file, String title) throws IOException {
        // 1. 上传到 RustFS
        String fileKey = storageService.generateKey(tenantId, "knowledge", file.getOriginalFilename());
        storageService.upload(fileKey, file.getInputStream(),
                file.getContentType(), file.getSize());

        // 2. 保存文档记录
        DocumentEntity doc = documentRepository.save(DocumentEntity.builder()
                .kbId(kbId)
                .tenantId(tenantId)
                .title(title != null ? title : file.getOriginalFilename())
                .fileKey(fileKey)
                .fileType(getFileExtension(file.getOriginalFilename()))
                .fileSize(file.getSize())
                .status("PENDING")
                .build());

        // 3. 异步处理（解析 → 分块 → 向量化）
        documentProcessService.processDocument(doc.getId(), tenantId, kbId);

        return doc;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "txt";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1) : "txt";
    }
}
