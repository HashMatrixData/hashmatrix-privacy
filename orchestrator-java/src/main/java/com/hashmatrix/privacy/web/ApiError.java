package com.hashmatrix.privacy.web;

/**
 * 统一错误体，对应契约 {@code PsiError}（code + message）。
 */
public record ApiError(String code, String message) {
}
