package com.tiocantinas.senac.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.tiocantinas.senac.model.LancamentoConta;
import com.tiocantinas.senac.model.enums.TipoLancamento;
import java.util.Optional;

public interface LancamentoContaRepository
        extends JpaRepository<LancamentoConta, Long>, JpaSpecificationExecutor<LancamentoConta> {

    Page<LancamentoConta> findByClienteId(Long clienteId, Pageable pageable);

    Optional<LancamentoConta> findByPedidoIdAndTipoLancamento(Long pedidoId, TipoLancamento tipoLancamento);
}
