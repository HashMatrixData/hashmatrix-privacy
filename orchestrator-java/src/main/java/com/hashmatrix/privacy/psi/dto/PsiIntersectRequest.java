package com.hashmatrix.privacy.psi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 编排层对外的 PSI 求交请求。
 *
 * <p>发起方租户不在请求体内，而是取自 {@code X-Tenant-Id} 头（经 TenantContext），
 * 避免客户端伪造租户身份。
 *
 * @param jobId                  作业标识
 * @param parties                参与方（恰好 2 方）
 * @param crossTenantAuthorized  跨租户联合计算的显式授权；默认 false（不串）
 */
public record PsiIntersectRequest(
        @NotBlank String jobId,
        @NotNull @Size(min = 2, max = 2) @Valid List<PartyDto> parties,
        boolean crossTenantAuthorized) {
}
