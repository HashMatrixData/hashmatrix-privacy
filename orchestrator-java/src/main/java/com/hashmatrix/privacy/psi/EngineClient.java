package com.hashmatrix.privacy.psi;

import com.hashmatrix.privacy.psi.dto.EngineRunRequest;
import com.hashmatrix.privacy.psi.dto.PsiResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 计算引擎 {@code engine-py} 的 REST 客户端。
 * 契约：{@code POST /v1/psi/run}（主仓 contracts/openapi/privacy-psi-v1.yaml）。
 */
@Component
public class EngineClient {

    private final RestClient restClient;

    public EngineClient(RestClient engineRestClient) {
        this.restClient = engineRestClient;
    }

    public PsiResult runPsi(EngineRunRequest request) {
        return restClient.post()
                .uri("/v1/psi/run")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PsiResult.class);
    }
}
