package com.hashmatrix.privacy.psi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hashmatrix.privacy.psi.dto.EngineRunRequest;
import com.hashmatrix.privacy.psi.dto.PartyDto;
import com.hashmatrix.privacy.psi.dto.PsiIntersectRequest;
import com.hashmatrix.privacy.psi.dto.PsiResult;
import io.hashmatrix.starter.tenant.TenantContext;
import io.hashmatrix.starter.tenant.TenantContextHolder;
import io.hashmatrix.starter.tenant.TenantContextMissingException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** PSI 编排服务单测：多租户守卫 + 引擎透传。脱敏占位 acme / tenant-demo / example.com。 */
@ExtendWith(MockitoExtension.class)
class PsiOrchestrationServiceTest {

    @Mock
    EngineClient engineClient;

    @AfterEach
    void clearTenant() {
        TenantContextHolder.clear();
    }

    private PsiIntersectRequest request(String tenantA, String tenantB, boolean authorized) {
        return new PsiIntersectRequest(
                "job-0001",
                List.of(
                        new PartyDto("party-a", tenantA, List.of("alice@example.com", "bob@example.com")),
                        new PartyDto("party-b", tenantB, List.of("bob@example.com", "carol@example.com"))),
                authorized);
    }

    @Test
    void sameTenantPassesThroughToEngine() {
        TenantContextHolder.set(TenantContext.of("tenant-demo"));
        PsiResult engineResult = new PsiResult("job-0001", "SUCCEEDED", 1, List.of("bob@example.com"), "ok");
        when(engineClient.runPsi(any())).thenReturn(engineResult);

        PsiResult result = new PsiOrchestrationService(engineClient)
                .intersect(request("tenant-demo", "tenant-demo", false));

        assertThat(result).isEqualTo(engineResult);
        ArgumentCaptor<EngineRunRequest> captor = ArgumentCaptor.forClass(EngineRunRequest.class);
        verify(engineClient).runPsi(captor.capture());
        // 发起方租户由上下文注入，而非客户端请求体
        assertThat(captor.getValue().initiatorTenant()).isEqualTo("tenant-demo");
    }

    @Test
    void crossTenantWithoutAuthorizationIsRejected() {
        TenantContextHolder.set(TenantContext.of("acme"));

        assertThatThrownBy(() -> new PsiOrchestrationService(engineClient)
                .intersect(request("acme", "tenant-demo", false)))
                .isInstanceOf(CrossTenantNotAuthorizedException.class);

        verify(engineClient, never()).runPsi(any());
    }

    @Test
    void crossTenantWithAuthorizationCallsEngine() {
        TenantContextHolder.set(TenantContext.of("acme"));
        when(engineClient.runPsi(any()))
                .thenReturn(new PsiResult("job-0001", "SUCCEEDED", 1, List.of("bob@example.com"), "ok"));

        new PsiOrchestrationService(engineClient).intersect(request("acme", "tenant-demo", true));

        verify(engineClient).runPsi(any());
    }

    @Test
    void missingTenantContextFails() {
        assertThatThrownBy(() -> new PsiOrchestrationService(engineClient)
                .intersect(request("acme", "acme", false)))
                .isInstanceOf(TenantContextMissingException.class);
    }
}
