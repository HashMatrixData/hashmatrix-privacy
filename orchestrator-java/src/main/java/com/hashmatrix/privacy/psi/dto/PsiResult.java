package com.hashmatrix.privacy.psi.dto;

import java.util.List;

/**
 * PSI 求交结果，对应契约 {@code PsiRunResult}（privacy-psi-v1.yaml）。
 *
 * @param jobId            作业标识
 * @param status           SUCCEEDED / REJECTED / FAILED
 * @param intersectionSize 交集基数
 * @param intersection     交集元素（mock 后端返回；真实后端可仅返回基数）
 * @param message          说明
 */
public record PsiResult(
        String jobId,
        String status,
        int intersectionSize,
        List<String> intersection,
        String message) {
}
