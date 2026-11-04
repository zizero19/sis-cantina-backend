package com.tiocantinas.senac.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.tiocantinas.senac.model.LancamentoConta;
import com.tiocantinas.senac.model.enums.TipoLancamento;

public class LancamentoContaSpecification {

    public static Specification<LancamentoConta> comCliente(Long clienteId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("cliente").get("id"), clienteId);
    }

    public static Specification<LancamentoConta> comTipo(TipoLancamento tipoLancamento) {
        return (root, query, criteriaBuilder) -> {
            if (tipoLancamento == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("tipoLancamento"),
                    tipoLancamento);
        };
    }

    public static Specification<LancamentoConta> comDataInicial(LocalDate dataInicial) {
        return (root, query, criteriaBuilder) -> {
            if (dataInicial == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.<LocalDateTime>get("dataHora"),
                    dataInicial.atStartOfDay());
        };
    }

    public static Specification<LancamentoConta> comDataFinal(LocalDate dataFinal) {
        return (root, query, criteriaBuilder) -> {
            if (dataFinal == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThan(
                    root.<LocalDateTime>get("dataHora"),
                    dataFinal.plusDays(1).atStartOfDay());
        };
    }
}
