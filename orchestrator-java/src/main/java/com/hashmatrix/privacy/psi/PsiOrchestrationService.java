package com.hashmatrix.privacy.psi;

import com.hashmatrix.privacy.psi.dto.EngineRunRequest;
import com.hashmatrix.privacy.psi.dto.PartyDto;
import com.hashmatrix.privacy.psi.dto.PsiIntersectRequest;
import com.hashmatrix.privacy.psi.dto.PsiResult;
import io.hashmatrix.starter.tenant.TenantContextHolder;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * PSI 编排：多租户边界守卫（第一道闸）+ 调用计算引擎。
 *
 * <p>红线：跨租户联合计算须显式授权，默认不串。编排层在此拦截，引擎侧再做纵深防御。
 */
@Service
public class PsiOrchestrationService {

    private final EngineClient engineClient;

    public PsiOrchestrationService(EngineClient engineClient) {
        this.engineClient = engineClient;
    }

    public PsiResult intersect(PsiIntersectRequest request) {
        // 缺租户上下文（无 X-Tenant-Id）时抛 TenantContextMissingException，由 handler 映射 400
        String initiatorTenant = TenantContextHolder.requireTenantId();

        assertCrossTenantAuthorized(request);

        EngineRunRequest engineRequest = new EngineRunRequest(
                request.jobId(),
                initiatorTenant,
                request.parties(),
                request.crossTenantAuthorized());
        return engineClient.runPsi(engineRequest);
    }

    private void assertCrossTenantAuthorized(PsiIntersectRequest request) {
        Set<String> tenants = request.parties().stream()
                .map(PartyDto::tenant)
                .collect(Collectors.toSet());
        if (tenants.size() > 1 && !request.crossTenantAuthorized()) {
            throw new CrossTenantNotAuthorizedException(
                    "cross-tenant PSI across " + tenants.stream().sorted().collect(Collectors.toList())
                            + " requires explicit authorization");
        }
    }
}
