package com.tiocantinas.senac.specification;

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
}
