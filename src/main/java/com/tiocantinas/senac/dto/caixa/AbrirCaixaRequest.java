package com.tiocantinas.senac.dto.caixa;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AbrirCaixaRequest(

        @NotBlank(message = "Responsavel pela abertura nao pode estar vazio") String responsavelAbertura,
        @NotNull(message = "Valor de abertura nao pode estar vazio") @PositiveOrZero(message = "Valor de abertura precisa ser maior ou igual a zero") BigDecimal valorAbertura,
        String observacoes) {

}
