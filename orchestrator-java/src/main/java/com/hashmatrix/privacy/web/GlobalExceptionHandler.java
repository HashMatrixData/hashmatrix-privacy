package com.hashmatrix.privacy.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashmatrix.privacy.psi.CrossTenantNotAuthorizedException;
import io.hashmatrix.starter.tenant.TenantContextMissingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * 异常 → 契约错误体映射。与 engine-py 的 PsiError 错误码对齐。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(CrossTenantNotAuthorizedException.class)
    public ResponseEntity<ApiError> handleCrossTenant(CrossTenantNotAuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("CROSS_TENANT_NOT_AUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(TenantContextMissingException.class)
    public ResponseEntity<ApiError> handleMissingTenant(TenantContextMissingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("INVALID_REQUEST", "missing tenant context (X-Tenant-Id header): " + ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("INVALID_REQUEST", message));
    }

    /**
     * 引擎返回的非 2xx（如 403 跨租户、422 非法请求）：原样透传状态码与结构化错误体；
     * 错误体无法解析时归一为 ENGINE_ERROR。避免裸抛 500、丢失契约错误语义。
     */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiError> handleEngineResponse(RestClientResponseException ex) {
        ApiError body;
        try {
            body = objectMapper.readValue(ex.getResponseBodyAsString(), ApiError.class);
        } catch (Exception parseFailure) {
            body = new ApiError("ENGINE_ERROR", ex.getResponseBodyAsString());
        }
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /** 引擎不可达（连接失败/超时）：502 ENGINE_ERROR。 */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiError> handleEngineUnreachable(ResourceAccessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("ENGINE_ERROR", "privacy engine unavailable: " + ex.getMessage()));
    }
}
