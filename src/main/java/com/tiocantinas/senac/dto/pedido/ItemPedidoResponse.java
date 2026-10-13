package com.tiocantinas.senac.dto.pedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long id,
        Long produtoId,
        String nomeProduto,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal) {
}
