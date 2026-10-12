package com.tiocantinas.senac.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tiocantinas.senac.model.enums.TipoLancamento;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lancamento_conta")
@NoArgsConstructor
@Getter
public class LancamentoConta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_lancamento", nullable = false)
    private TipoLancamento tipoLancamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 255)
    private String descricao;

    public LancamentoConta(
            Cliente cliente,
            Pedido pedido,
            TipoLancamento tipoLancamento,
            BigDecimal valor,
            String descricao) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "O cliente é obrigatório.");
        }

        if (tipoLancamento == null) {
            throw new IllegalArgumentException(
                    "O tipo do lançamento é obrigatório.");
        }

        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException(
                    "O valor do lançamento deve ser maior que zero.");
        }

        this.cliente = cliente;
        this.pedido = pedido;
        this.tipoLancamento = tipoLancamento;
        this.valor = valor;
        this.descricao = descricao;
        this.dataHora = LocalDateTime.now();
    }
}