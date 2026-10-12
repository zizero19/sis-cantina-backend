package com.tiocantinas.senac.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tiocantinas.senac.model.enums.CategoriaProduto;
import com.tiocantinas.senac.model.enums.UnidadeMedida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produto")
@NoArgsConstructor
@Getter
@Setter
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCusto;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal estoqueMinimo = BigDecimal.ZERO;

    private LocalDate dataValidade;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidadeEstoque;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProduto categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnidadeMedida unidadeMedida;

    public void baixarEstoque(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero.");
        }

        if (quantidadeEstoque.compareTo(quantidade) < 0) {
            throw new IllegalStateException(
                    "Estoque insuficiente para o produto.");
        }

        quantidadeEstoque = quantidadeEstoque.subtract(quantidade);
    }

    public void reporEstoque(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero.");
        }

        quantidadeEstoque = quantidadeEstoque.add(quantidade);
    }

}
