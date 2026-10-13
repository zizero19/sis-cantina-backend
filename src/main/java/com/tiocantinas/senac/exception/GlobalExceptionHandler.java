package com.tiocantinas.senac.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tiocantinas.senac.dto.error.ErrorResponse;
import com.tiocantinas.senac.dto.error.ValidationErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ProdutoNaoEncontradoException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ErrorResponse handleProdutoNaoEncontradoException(
                        ProdutoNaoEncontradoException exception) {

                return criarErrorResponse(
                                HttpStatus.NOT_FOUND,
                                exception);
        }

        @ExceptionHandler(ClienteNaoEncontradoException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ErrorResponse handleClienteNaoEncontradoException(
                        ClienteNaoEncontradoException exception) {

                return criarErrorResponse(
                                HttpStatus.NOT_FOUND,
                                exception);
        }

        @ExceptionHandler(SetorNaoEncontradoException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ErrorResponse handleSetorNaoEncontradoException(
                        SetorNaoEncontradoException exception) {

                return criarErrorResponse(
                                HttpStatus.NOT_FOUND,
                                exception);
        }

        @ExceptionHandler(SetorJaExisteException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public ErrorResponse handleSetorJaExisteException(
                        SetorJaExisteException exception) {

                return criarErrorResponse(
                                HttpStatus.CONFLICT,
                                exception);
        }

        @ExceptionHandler(PedidoNaoEncontradoException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ErrorResponse handlePedidoNaoEncontradoException(
                        PedidoNaoEncontradoException exception) {

                return criarErrorResponse(
                                HttpStatus.NOT_FOUND,
                                exception);
        }

        // Trata erros de validação de dados, como campos obrigatórios ou formatos
        // inválidos
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ValidationErrorResponse handleValidationException(
                        MethodArgumentNotValidException exception) {
                Map<String, String> campos = new LinkedHashMap<>();

                exception.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> campos.put(error.getField(), error.getDefaultMessage()));

                return new ValidationErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "Erro de validação de dados",
                                campos,
                                LocalDateTime.now());
        }

        private ErrorResponse criarErrorResponse(
                        HttpStatus status,
                        Exception exception) {

                return new ErrorResponse(
                                status.value(),
                                status.getReasonPhrase(),
                                exception.getMessage(),
                                LocalDateTime.now());
        }
}
