package com.tiocantinas.senac.dto.pedido;

import jakarta.validation.constraints.Size;

public record PedidoRequest(

        @Size(max = 255) String observacoes

) {
}