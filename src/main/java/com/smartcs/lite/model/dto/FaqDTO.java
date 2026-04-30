package com.smartcs.lite.model.dto;

import jakarta.validation.constraints.NotBlank;

public record FaqDTO(
        Long id,
        @NotBlank(message = "问题不能为空") String question,
        @NotBlank(message = "答案不能为空") String answer,
        String category
) {}
