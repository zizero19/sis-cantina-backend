package com.tiocantinas.senac.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.tiocantinas.senac.model.Caixa;
import com.tiocantinas.senac.model.enums.StatusCaixa;

public class CaixaSpecification {

    public static Specification<Caixa> comStatus(StatusCaixa status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status);
        };
    }

    public static Specification<Caixa> comResponsavel(String responsavel) {
        return (root, query, criteriaBuilder) -> {
            if (responsavel == null || responsavel.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String texto = responsavel.trim()
                    .toLowerCase(Locale.ROOT)
                    .replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.<String>get("responsavelAbertura")),
                    "%" + texto + "%",
                    '!');
        };
    }

    public static Specification<Caixa> comDataInicial(LocalDate dataInicial) {
        return (root, query, cb) -> {
            if (dataInicial == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.<LocalDateTime>get("abertura"),
                    dataInicial.atStartOfDay());
        };
    }

    public static Specification<Caixa> comDataFinal(LocalDate dataFinal) {
        return (root, query, cb) -> {
            if (dataFinal == null) {
                return cb.conjunction();
            }

            return cb.lessThan(
                    root.<LocalDateTime>get("abertura"),
                    dataFinal.plusDays(1).atStartOfDay());
        };
    }

}
