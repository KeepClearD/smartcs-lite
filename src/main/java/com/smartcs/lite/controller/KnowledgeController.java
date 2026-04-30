package com.smartcs.lite.controller;

import com.smartcs.lite.common.Result;
import com.smartcs.lite.interceptor.TenantContext;
import com.smartcs.lite.model.dto.FaqDTO;
import com.smartcs.lite.model.entity.DocumentEntity;
import com.smartcs.lite.model.entity.Faq;
import com.smartcs.lite.model.entity.KnowledgeBase;
import com.smartcs.lite.service.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge-bases")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping
    public Result<List<KnowledgeBase>> list() {
        return Result.ok(knowledgeService.listKnowledgeBases(TenantContext.get()));
    }

    @PostMapping
    public Result<KnowledgeBase> create(
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        return Result.ok(knowledgeService.createKnowledgeBase(TenantContext.get(), name, description));
    }

    // ==================== FAQ ====================

    @GetMapping("/{kbId}/faqs")
    public Result<List<Faq>> listFaqs(@PathVariable Long kbId) {
        return Result.ok(knowledgeService.listFaqs(kbId, TenantContext.get()));
    }

    @PostMapping("/{kbId}/faqs")
    public Result<Void> addFaq(@PathVariable Long kbId, @RequestBody @Valid FaqDTO dto) {
        knowledgeService.addFaq(kbId, TenantContext.get(), dto.question(), dto.answer(), dto.category());
        return Result.ok();
    }

    @DeleteMapping("/{kbId}/faqs/{faqId}")
    public Result<Void> deleteFaq(@PathVariable Long kbId, @PathVariable Long faqId) {
        knowledgeService.deleteFaq(faqId);
        return Result.ok();
    }

    // ==================== 文档 ====================

    @GetMapping("/{kbId}/documents")
    public Result<List<DocumentEntity>> listDocuments(@PathVariable Long kbId) {
        return Result.ok(knowledgeService.listDocuments(kbId, TenantContext.get()));
    }

    @PostMapping("/{kbId}/documents")
    public Result<DocumentEntity> uploadDocument(
            @PathVariable Long kbId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title) throws IOException {
        return Result.ok(knowledgeService.uploadDocument(kbId, TenantContext.get(), file, title));
    }
}
