package com.tiocantinas.senac.dto.caixa;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FecharCaixaRequest(

        @NotNull(message = "Valor de fechamento não pode estar vazio") @PositiveOrZero(message = "Valor de fechamento deve ser maior ou igual a zero") BigDecimal valorFechamento) {

}
