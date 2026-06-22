package com.hashmatrix.privacy.config;

import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 计算引擎 {@code engine-py} 的 RestClient 配置。基址由 {@code engine.base-url} 注入
 * （docker-compose 内为 http://engine-py:8087，本地直跑为 http://localhost:8087）。
 *
 * <p>底层 JDK HttpClient <b>钉死 HTTP/1.1</b>：其默认会尝试 h2c（HTTP/2 cleartext）升级，
 * 而引擎侧 uvicorn/h11 仅支持 HTTP/1.1，升级握手会导致请求体被丢弃（FastAPI 收到空 body）。
 */
@Configuration
public class EngineClientConfig {

    @Bean
    public RestClient engineRestClient(@Value("${engine.base-url:http://localhost:8087}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
