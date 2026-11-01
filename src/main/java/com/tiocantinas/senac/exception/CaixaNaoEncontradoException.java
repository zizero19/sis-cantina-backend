package com.tiocantinas.senac.exception;

public class CaixaNaoEncontradoException extends RuntimeException {

    public CaixaNaoEncontradoException(Long id) {
        super("Caixa não encontrado: " + id + "não foi encontrado");
    }
}
