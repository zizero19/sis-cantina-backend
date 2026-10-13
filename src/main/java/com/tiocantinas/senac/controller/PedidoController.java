package com.tiocantinas.senac.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tiocantinas.senac.dto.pedido.AdicionarItemPedidoRequest;
import com.tiocantinas.senac.dto.pedido.PedidoRequest;
import com.tiocantinas.senac.dto.pedido.PedidoResponse;
import com.tiocantinas.senac.model.enums.FormaPagamento;
import com.tiocantinas.senac.service.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pedido")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criarPedido(@Valid @RequestBody PedidoRequest request) {
        return pedidoService.criar(request);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscarPedidoPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @GetMapping
    public List<PedidoResponse> listarPedidos() {
        return pedidoService.listarTodos();
    }

    @PostMapping("/{id}/itens")
    public PedidoResponse adicionarItem(
            @PathVariable Long id,
            @Valid @RequestBody AdicionarItemPedidoRequest request) {
        return pedidoService.adicionarItem(id, request);
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public PedidoResponse removerItem(
            @PathVariable Long id,
            @PathVariable Long itemId) {

        return pedidoService.removerItem(id, itemId);
    }

    @PutMapping("/{id}/cliente")
    public PedidoResponse associarCliente(
            @PathVariable Long id,
            @RequestBody String cpf) {

        return pedidoService.associarCliente(id, cpf);
    }

    @PutMapping("/{id}/forma-pagamento")
    public PedidoResponse definirFormaPagamento(
            @PathVariable Long id,
            @RequestBody FormaPagamento formaPagamento) {

        return pedidoService.definirFormaPagamento(id, formaPagamento);
    }

    @PatchMapping("/{id}/aguardar-pagamento")
    public PedidoResponse aguardarPagamento(@PathVariable Long id) {
        return pedidoService.aguardarPagamento(id);
    }

    @PatchMapping("/{id}/finalizar")
    public PedidoResponse finalizarPedido(@PathVariable Long id) {
        return pedidoService.finalizar(id);
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelarPedido(@PathVariable Long id) {
        return pedidoService.cancelar(id);
    }
}
