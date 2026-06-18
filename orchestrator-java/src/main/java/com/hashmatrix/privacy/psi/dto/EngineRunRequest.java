package com.hashmatrix.privacy.psi.dto;

import java.util.List;

/**
 * 发往计算引擎 {@code engine-py} 的 PSI 运行请求，对应契约
 * {@code POST /v1/psi/run}（privacy-psi-v1.yaml）。
 *
 * @param jobId                 作业标识
 * @param initiatorTenant       发起方租户（由编排层从 TenantContext 注入）
 * @param parties               参与方
 * @param crossTenantAuthorized 跨租户显式授权
 */
public record EngineRunRequest(
        String jobId,
        String initiatorTenant,
        List<PartyDto> parties,
        boolean crossTenantAuthorized) {
}
