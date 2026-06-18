package com.hashmatrix.privacy.psi;

/**
 * 跨租户联合计算未显式授权（默认不串）。映射为 HTTP 403。
 */
public class CrossTenantNotAuthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CrossTenantNotAuthorizedException(String message) {
        super(message);
    }
}
