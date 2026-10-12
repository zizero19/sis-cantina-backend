package com.tiocantinas.senac.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.tiocantinas.senac.model.enums.TipoLancamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@NoArgsConstructor
@Getter
@Setter
public class Cliente extends Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cliente")
    private List<LancamentoConta> lancamentos = new ArrayList<>();

    public boolean estaAtivo() {
        return Boolean.TRUE.equals(ativo);
    }

    public BigDecimal calcularSaldoDevedor() {
        BigDecimal debitos = lancamentos.stream()
                .filter(lancamento -> lancamento.getTipoLancamento() == TipoLancamento.DEBITO)
                .map(LancamentoConta::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditos = lancamentos.stream()
                .filter(lancamento -> lancamento.getTipoLancamento() == TipoLancamento.CREDITO)
                .map(LancamentoConta::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldo = debitos.subtract(creditos);

        if (saldo.signum() < 0) {
            return BigDecimal.ZERO;
        }

        return saldo;
    }

    public BigDecimal calcularCreditoDisponivel() {
        BigDecimal creditoDisponivel = limiteCredito.subtract(calcularSaldoDevedor());

        if (creditoDisponivel.signum() < 0) {
            return BigDecimal.ZERO;
        }

        return creditoDisponivel.min(limiteCredito);
    }

    public boolean possuiCreditoDisponivel(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) {
            return false;
        }

        return calcularCreditoDisponivel()
                .compareTo(valor) >= 0;
    }
}
