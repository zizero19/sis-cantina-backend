package com.tiocantinas.senac.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.tiocantinas.senac.model.Caixa;
import com.tiocantinas.senac.model.enums.StatusCaixa;

public interface CaixaRepository extends
        JpaRepository<Caixa, Long>,
        JpaSpecificationExecutor<Caixa> {

    Optional<Caixa> findByStatus(StatusCaixa status);

    boolean existsByStatus(StatusCaixa status);

    Page<Caixa> findAllByStatus(StatusCaixa status, Pageable pageable);
}
