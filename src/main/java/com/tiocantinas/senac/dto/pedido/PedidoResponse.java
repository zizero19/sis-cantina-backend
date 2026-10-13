
package com.tiocantinas.senac.dto.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tiocantinas.senac.model.enums.StatusPedido;
import com.tiocantinas.senac.model.enums.FormaPagamento;

public record PedidoResponse(

        Long id,
        LocalDateTime dataHora,
        StatusPedido statusPedido,
        String observacoes,
        BigDecimal precoTotal,
        FormaPagamento formaPagamento,
        Long clienteId,
        Long caixaId

) {
}
