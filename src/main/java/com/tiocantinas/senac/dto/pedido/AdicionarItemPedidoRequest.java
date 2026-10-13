package com.tiocantinas.senac.dto.pedido;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdicionarItemPedidoRequest(
        @NotNull Long produtoId,

        @NotNull @Positive BigDecimal quantidade) {
}
