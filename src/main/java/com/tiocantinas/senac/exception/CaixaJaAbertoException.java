package com.tiocantinas.senac.exception;

public class CaixaJaAbertoException extends RuntimeException {
    public CaixaJaAbertoException() {
        super("Caixa aberto: já existe um caixa aberto");
    }
}
