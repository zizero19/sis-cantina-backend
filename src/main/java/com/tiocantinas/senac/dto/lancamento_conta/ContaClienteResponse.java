package com.tiocantinas.senac.dto.lancamento_conta;

import java.math.BigDecimal;

public record ContaClienteResponse(
        Long clienteId,
        String nomeCliente,
        String cpf,
        BigDecimal limiteCredito,
        BigDecimal saldoDevedor,
        BigDecimal creditoDisponivel) {
}
