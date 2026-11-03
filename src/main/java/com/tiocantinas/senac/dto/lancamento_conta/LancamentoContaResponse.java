package com.tiocantinas.senac.dto.lancamento_conta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tiocantinas.senac.model.enums.TipoLancamento;

public record LancamentoContaResponse(
        Long id,
        Long clienteId,
        Long pedidoId,
        TipoLancamento tipoLancamento,
        BigDecimal valor,
        LocalDateTime dataHora,
        String descricao) {

}
