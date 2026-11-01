package com.tiocantinas.senac.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tiocantinas.senac.model.enums.StatusCaixa;
import com.tiocantinas.senac.model.enums.StatusPedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "caixa")
@NoArgsConstructor
@Getter
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "caixa")
    private List<Pedido> pedidos = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String responsavelAbertura;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAbertura;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorFechamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDinheiro = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCartao = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPix = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAPrazo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCaixa status = StatusCaixa.ABERTO;

    @Column(nullable = false)
    private LocalDateTime abertura;

    private LocalDateTime fechamento;

    @Column(length = 255)
    private String observacoes;

    public Caixa(String responsavelAbertura,
            BigDecimal valorAbertura,
            String observacoes) {

        this.responsavelAbertura = responsavelAbertura;
        this.valorAbertura = valorAbertura;
        this.observacoes = observacoes;
        this.abertura = LocalDateTime.now();
    }

    public void registrarPedido(Pedido pedido) {
        validarAberto();

        if (pedido == null) {
            throw new IllegalArgumentException("O pedido é obrigatório.");
        }

        if (pedido.getStatusPedido() != StatusPedido.FINALIZADO)
            throw new IllegalStateException("Somente pedidos finalizados podem ser registrados no caixa.");

        if (pedido.getCaixa() != null)
            throw new IllegalStateException("O pedido já foi registrado em um caixa.");

        if (pedido.getFormaPagamento() == null) {
            throw new IllegalStateException("A forma de pagamento do pedido é obrigatória.");
        }

        pedido.definirCaixa(this);
        pedidos.add(pedido);
        atualizarTotal(pedido, pedido.getPrecoTotal());
    }

    void estornarPedido(Pedido pedido) {
        validarAberto();

        if (pedido == null || pedido.getCaixa() == null
                || (pedido.getCaixa() != this && (id == null || !id.equals(pedido.getCaixa().getId())))) {
            throw new IllegalArgumentException("O pedido não pertence a este caixa.");
        }

        if (pedido.getStatusPedido() != StatusPedido.FINALIZADO) {
            throw new IllegalStateException("Somente pedidos finalizados podem ser estornados.");
        }

        atualizarTotal(pedido, pedido.getPrecoTotal().negate());
    }

    private void atualizarTotal(Pedido pedido, BigDecimal valor) {
        switch (pedido.getFormaPagamento()) {
            case DINHEIRO -> totalDinheiro = totalDinheiro.add(valor);
            case DEBITO, CREDITO -> totalCartao = totalCartao.add(valor);
            case PIX -> totalPix = totalPix.add(valor);
            case A_PRAZO -> totalAPrazo = totalAPrazo.add(valor);
        }
    }

    private void validarAberto() {
        if (status != StatusCaixa.ABERTO) {
            throw new IllegalStateException("Esta operação exige um caixa aberto.");
        }
    }

    public List<Pedido> getPedidos() {
        return Collections.unmodifiableList(pedidos);
    }

    public void fechar(BigDecimal valorFechamento) {
        if (status == StatusCaixa.FECHADO)
            throw new IllegalStateException("O caixa já está fechado.");

        if (valorFechamento == null || valorFechamento.signum() < 0)
            throw new IllegalArgumentException("O valor de fechamento não pode ser negativo.");

        this.valorFechamento = valorFechamento;
        this.fechamento = LocalDateTime.now();
        this.status = StatusCaixa.FECHADO;
    }

    public BigDecimal calcularValorEsperado() {
        return valorAbertura.add(totalDinheiro);
    }

    public BigDecimal calcularDiferenca() {
        if (valorFechamento == null)
            throw new IllegalStateException("O caixa ainda não foi fechado.");

        return valorFechamento.subtract(calcularValorEsperado());
    }
}
