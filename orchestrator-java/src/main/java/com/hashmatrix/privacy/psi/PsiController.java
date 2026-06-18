package com.hashmatrix.privacy.psi;

import com.hashmatrix.privacy.psi.dto.PsiIntersectRequest;
import com.hashmatrix.privacy.psi.dto.PsiResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PSI 编排对外 REST 接口。租户身份取自 {@code X-Tenant-Id} 头（经 TenantContextFilter）。
 */
@RestController
@RequestMapping("/api/v1/psi")
public class PsiController {

    private final PsiOrchestrationService orchestrationService;

    public PsiController(PsiOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/intersect")
    public PsiResult intersect(@Valid @RequestBody PsiIntersectRequest request) {
        return orchestrationService.intersect(request);
    }
}
