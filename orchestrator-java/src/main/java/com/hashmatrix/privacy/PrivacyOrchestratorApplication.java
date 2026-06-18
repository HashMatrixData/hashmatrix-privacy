package com.hashmatrix.privacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 隐私计算编排层入口。
 *
 * <p>职责：接收编排请求 → 多租户边界守卫（跨租户须显式授权）→ 调用计算引擎
 * {@code engine-py} 执行 PSI 等隐私计算原语。引擎契约见
 * {@code contracts/openapi/privacy-psi-v1.yaml}。
 */
@SpringBootApplication
public class PrivacyOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrivacyOrchestratorApplication.class, args);
    }
}
