package com.tiocantinas.senac.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tiocantinas.senac.dto.lancamento_conta.ContaClienteResponse;
import com.tiocantinas.senac.dto.lancamento_conta.LancamentoContaResponse;
import com.tiocantinas.senac.dto.lancamento_conta.RegistrarPagamentoRequest;
import com.tiocantinas.senac.model.enums.TipoLancamento;
import com.tiocantinas.senac.service.LancamentoContaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/lancamento-conta")
@RequiredArgsConstructor
public class LancamentoContaController {

    // TODO: Integrar o LancamentoConta no PedidoService

    private final LancamentoContaService lancamentoContaService;

    @PostMapping("/clientes/{clienteId}/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public LancamentoContaResponse registrarPagamento(
            @PathVariable Long clienteId,
            @RequestBody @Valid RegistrarPagamentoRequest request) {

        return lancamentoContaService.registrarPagamento(clienteId, request);
    }

    @GetMapping("/{id}")
    public LancamentoContaResponse buscarPorId(@PathVariable Long id) {
        return lancamentoContaService.buscarPorId(id);
    }

    @GetMapping("/clientes/{clienteId}/resumo")
    public ContaClienteResponse consultarConta(
            @PathVariable Long clienteId) {

        return lancamentoContaService.consultarConta(clienteId);
    }

    @GetMapping("/clientes/{clienteId}")
    public Page<LancamentoContaResponse> listarPorCliente(
            @PathVariable Long clienteId,

            @PageableDefault(size = 10, sort = { "dataHora", "id" }, direction = Direction.DESC)

            Pageable pageable,

            @RequestParam(required = false) TipoLancamento tipoLancamento,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        return lancamentoContaService.listarPorCliente(
                clienteId,
                pageable,
                tipoLancamento,
                dataInicial,
                dataFinal);
    }

}
