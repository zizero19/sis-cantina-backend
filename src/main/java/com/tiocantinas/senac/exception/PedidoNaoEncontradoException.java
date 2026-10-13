package com.tiocantinas.senac.exception;

public class PedidoNaoEncontradoException extends RuntimeException {
    public PedidoNaoEncontradoException(Long id) {
        super("Pedido não encontrado: " + id + " não foi encontrado.");
    }
}
