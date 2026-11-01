package com.tiocantinas.senac.dto.caixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tiocantinas.senac.model.enums.StatusCaixa;

public record CaixaResponse(
                Long id,
                String responsavelAbertura,
                BigDecimal valorAbertura,
                BigDecimal valorFechamento,
                BigDecimal totalDinheiro,
                BigDecimal totalCartao,
                BigDecimal totalPix,
                BigDecimal totalAPrazo,
                BigDecimal valorEsperado,
                BigDecimal diferenca,
                StatusCaixa status,
                LocalDateTime abertura,
                LocalDateTime fechamento,
                String observacoes) {
}
