package com.hashmatrix.privacy.psi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hashmatrix.privacy.psi.dto.PsiResult;
import io.hashmatrix.starter.tenant.TenantContextFilter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.WebApplicationContext;

/**
 * 端到端 Web 测试：X-Tenant-Id 头 → TenantContextFilter → 编排服务。
 * EngineClient 被 mock，不依赖真实 engine-py。
 */
@SpringBootTest
class PsiControllerWebTest {

    // 与 starter-tenant TenantProperties.header 默认值一致（ICD 固定路由键）
    private static final String TENANT_HEADER = "X-Tenant-Id";

    @MockBean
    EngineClient engineClient;

    @Autowired
    WebApplicationContext context;

    @Autowired
    TenantContextFilter tenantContextFilter;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .addFilters(tenantContextFilter)
                .build();
    }

    private static final String SAME_TENANT_BODY = """
            {
              "jobId": "job-0001",
              "parties": [
                {"partyId": "party-a", "tenant": "tenant-demo", "elements": ["alice@example.com","bob@example.com"]},
                {"partyId": "party-b", "tenant": "tenant-demo", "elements": ["bob@example.com","carol@example.com"]}
              ],
              "crossTenantAuthorized": false
            }
            """;

    private static final String CROSS_TENANT_BODY = """
            {
              "jobId": "job-0001",
              "parties": [
                {"partyId": "party-a", "tenant": "acme", "elements": ["alice@example.com","bob@example.com"]},
                {"partyId": "party-b", "tenant": "tenant-demo", "elements": ["bob@example.com","carol@example.com"]}
              ],
              "crossTenantAuthorized": false
            }
            """;

    @Test
    void sameTenantReturns200() throws Exception {
        when(engineClient.runPsi(any()))
                .thenReturn(new PsiResult("job-0001", "SUCCEEDED", 1, List.of("bob@example.com"), "ok"));

        mockMvc().perform(post("/api/v1/psi/intersect")
                        .header(TENANT_HEADER, "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAME_TENANT_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intersectionSize").value(1))
                .andExpect(jsonPath("$.intersection[0]").value("bob@example.com"));
    }

    @Test
    void crossTenantWithoutAuthorizationReturns403() throws Exception {
        mockMvc().perform(post("/api/v1/psi/intersect")
                        .header(TENANT_HEADER, "acme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CROSS_TENANT_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CROSS_TENANT_NOT_AUTHORIZED"));
    }

    @Test
    void missingTenantHeaderReturns400() throws Exception {
        mockMvc().perform(post("/api/v1/psi/intersect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAME_TENANT_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void engineErrorIsPassedThroughWithContractBody() throws Exception {
        // 引擎返回 422 + 结构化 PsiError → 编排层原样透传状态码与错误码（不裸抛 500）
        String engineBody = "{\"code\":\"INVALID_REQUEST\",\"message\":\"PSI requires exactly 2 parties\"}";
        when(engineClient.runPsi(any())).thenThrow(
                HttpClientErrorException.create(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity",
                        org.springframework.http.HttpHeaders.EMPTY, engineBody.getBytes(), null));

        mockMvc().perform(post("/api/v1/psi/intersect")
                        .header(TENANT_HEADER, "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAME_TENANT_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void engineUnreachableReturns502() throws Exception {
        when(engineClient.runPsi(any())).thenThrow(new ResourceAccessException("connection refused"));

        mockMvc().perform(post("/api/v1/psi/intersect")
                        .header(TENANT_HEADER, "tenant-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SAME_TENANT_BODY))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("ENGINE_ERROR"));
    }

    @Test
    void invalidPartyCountReturns400() throws Exception {
        String oneParty = """
                {
                  "jobId": "job-0001",
                  "parties": [
                    {"partyId": "party-a", "tenant": "acme", "elements": ["alice@example.com"]}
                  ],
                  "crossTenantAuthorized": false
                }
                """;
        mockMvc().perform(post("/api/v1/psi/intersect")
                        .header(TENANT_HEADER, "acme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oneParty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
