package com.tiocantinas.senac.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tiocantinas.senac.model.enums.FormaPagamento;
import com.tiocantinas.senac.model.enums.StatusPedido;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedido")
@NoArgsConstructor
@Getter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pedido", nullable = false)
    private StatusPedido statusPedido = StatusPedido.CRIADO;

    @Column(length = 255)
    private String observacoes;

    @Column(name = "preco_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id")
    private Caixa caixa;

    public Pedido(String observacoes) {
        this.observacoes = observacoes;
    }

    public void adicionarItem(ItemPedido item) {
        validarPedidoEmCriacao();

        item.definirPedido(this);
        itens.add(item);

        recalcularTotal();
    }

    public void removerItem(ItemPedido item) {
        validarPedidoEmCriacao();

        if (!itens.remove(item)) {
            throw new IllegalArgumentException(
                    "O item não pertence a este pedido.");
        }

        item.removerPedido();

        recalcularTotal();
    }

    public void associarCliente(Cliente cliente) {
        validarPedidoEmCriacao();

        this.cliente = cliente;
    }

    public void removerCliente() {
        validarPedidoEmCriacao();

        this.cliente = null;
    }

    public void definirFormaPagamento(FormaPagamento formaPagamento) {
        validarPedidoEmCriacao();

        if (formaPagamento == null) {
            throw new IllegalArgumentException(
                    "A forma de pagamento é obrigatória.");
        }

        this.formaPagamento = formaPagamento;
    }

    public void aguardarPagamento() {
        validarPedidoEmCriacao();

        if (itens.isEmpty()) {
            throw new IllegalStateException(
                    "Não é possível prosseguir com um pedido sem itens.");
        }

        if (formaPagamento == null) {
            throw new IllegalStateException(
                    "A forma de pagamento deve ser informada.");
        }

        if (formaPagamento == FormaPagamento.A_PRAZO) {
            validarVendaAPrazo();
        }

        statusPedido = StatusPedido.AGUARDANDO_PAGAMENTO;
    }

    public void finalizarPedido() {
        if (statusPedido != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new IllegalStateException(
                    "Somente um pedido aguardando pagamento pode ser finalizado.");
        }

        statusPedido = StatusPedido.FINALIZADO;
    }

    public void cancelarPedido() {
        if (statusPedido == StatusPedido.CANCELADO) {
            throw new IllegalStateException(
                    "Um pedido já cancelado não pode ser cancelado novamente.");
        }

        statusPedido = StatusPedido.CANCELADO;
    }

    void definirCaixa(Caixa caixa) {
        if (this.caixa != null) {
            throw new IllegalStateException(
                    "O caixa já foi definido para este pedido.");
        }

        this.caixa = caixa;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    private void recalcularTotal() {
        precoTotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarVendaAPrazo() {
        if (cliente == null) {
            throw new IllegalStateException(
                    "Pedidos a prazo exigem um cliente.");
        }

        if (!cliente.estaAtivo()) {
            throw new IllegalStateException(
                    "Não é possível realizar venda a prazo para cliente inativo.");
        }

        if (!cliente.possuiCreditoDisponivel(precoTotal)) {
            throw new IllegalStateException(
                    "O cliente não possui limite de crédito disponível.");
        }
    }

    private void validarPedidoEmCriacao() {
        if (statusPedido != StatusPedido.CRIADO) {
            throw new IllegalStateException(
                    "Esta operação só pode ser realizada enquanto o pedido está em criação.");
        }
    }
}