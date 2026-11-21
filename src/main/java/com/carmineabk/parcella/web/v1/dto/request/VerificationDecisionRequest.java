package com.carmineabk.parcella.web.v1.dto.request;

import com.carmineabk.parcella.domain.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VerificationDecisionRequest(
        @NotNull VerificationStatus decision,
        @Size(max = 500) String note) {}