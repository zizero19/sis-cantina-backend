package com.tiocantinas.senac.dto.lancamento_conta;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistrarPagamentoRequest(
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal valor,

        @Size(max = 255) String descricao) {

}
