package com.hashmatrix.privacy.psi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * PSI 参与方。脱敏占位：{@code tenant-demo} / {@code acme} / {@code example.com}。
 *
 * @param partyId  参与方（节点）标识
 * @param tenant   参与方所属租户
 * @param elements 该方持有的求交元素（mock 明文；真实 PSI 不出域）
 */
public record PartyDto(
        @NotBlank String partyId,
        @NotBlank String tenant,
        @NotNull List<String> elements) {
}
