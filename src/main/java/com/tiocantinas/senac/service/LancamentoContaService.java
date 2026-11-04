package com.tiocantinas.senac.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiocantinas.senac.dto.lancamento_conta.ContaClienteResponse;
import com.tiocantinas.senac.dto.lancamento_conta.LancamentoContaResponse;
import com.tiocantinas.senac.dto.lancamento_conta.RegistrarPagamentoRequest;
import com.tiocantinas.senac.exception.ClienteNaoEncontradoException;
import com.tiocantinas.senac.exception.PedidoNaoEncontradoException;
import com.tiocantinas.senac.exception.PeriodoInvalidoException;
import com.tiocantinas.senac.model.Cliente;
import com.tiocantinas.senac.model.LancamentoConta;
import com.tiocantinas.senac.model.Pedido;
import com.tiocantinas.senac.model.enums.FormaPagamento;
import com.tiocantinas.senac.model.enums.StatusPedido;
import com.tiocantinas.senac.model.enums.TipoLancamento;
import com.tiocantinas.senac.repository.ClienteRepository;
import com.tiocantinas.senac.repository.LancamentoContaRepository;
import com.tiocantinas.senac.repository.PedidoRepository;
import com.tiocantinas.senac.specification.LancamentoContaSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LancamentoContaService {

    private final LancamentoContaRepository lancamentoContaRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public LancamentoContaResponse registrarDebitoDoPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        Cliente cliente = clienteRepository.findById(pedido.getCliente().getId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(pedido.getCliente().getId()));

        if (pedido.getStatusPedido() != StatusPedido.FINALIZADO
                || pedido.getFormaPagamento() != FormaPagamento.A_PRAZO) {
            throw new IllegalStateException(
                    "Somente pedidos finalizados a prazo podem gerar débito.");
        }

        if (pedido.getCliente() == null) {
            throw new IllegalStateException(
                    "O pedido a prazo precisa ter um cliente.");
        }

        if (!cliente.estaAtivo()) {
            throw new IllegalStateException(
                    "Não é possível registrar débito para cliente inativo.");
        }

        if (cliente.getLimiteCredito().compareTo(pedido.getPrecoTotal()) < 0) {
            throw new IllegalStateException(
                    "Cliente com o id " + cliente.getId()
                            + " não possui limite de crédito suficiente!");
        }

        if (cliente.getLancamentos().stream()
                .anyMatch(x -> x.getTipoLancamento() == TipoLancamento.DEBITO
                        && x.getPedido() != null
                        && x.getPedido().getId().equals(pedidoId))) {
            throw new IllegalStateException(
                    "Cliente com o id " + cliente.getId()
                            + " já possui um lançamento de débito para o pedido com o id "
                            + pedidoId + "!");
        }

        LancamentoConta debito = new LancamentoConta(
                cliente,
                pedido,
                TipoLancamento.DEBITO,
                pedido.getPrecoTotal(),
                "Débito referente ao pedido com o id " + pedidoId);

        LancamentoConta lancamentoSalvo = lancamentoContaRepository.save(debito);

        cliente.getLancamentos().add(lancamentoSalvo);

        return toResponse(lancamentoSalvo);
    }

    @Transactional
    public LancamentoContaResponse registrarPagamento(Long clienteId, RegistrarPagamentoRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

        BigDecimal saldoDevedor = cliente.calcularSaldoDevedor();

        if (request.valor().compareTo(saldoDevedor) > 0) {
            throw new IllegalStateException(
                    "O valor do pagamento não pode ser maior que a dívida do cliente");
        }

        LancamentoConta credito = new LancamentoConta(
                cliente,
                null,
                TipoLancamento.CREDITO,
                request.valor(),
                request.descricao());

        cliente.getLancamentos().add(credito);

        LancamentoConta lancamentoConta = lancamentoContaRepository.save(credito);

        return toResponse(lancamentoConta);
    }

    @Transactional
    public LancamentoContaResponse estornarDebitoDoPedido(Long pedidoId) {
        Pedido pedidoCancelado = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        if (pedidoCancelado.getStatusPedido() != StatusPedido.CANCELADO) {
            throw new IllegalStateException("Somente pedidos cancelados podem ter o débito estornado");
        }

        LancamentoConta debito = lancamentoContaRepository
                .findByPedidoIdAndTipoLancamento(pedidoId, TipoLancamento.DEBITO)
                .orElseThrow(() -> new IllegalStateException(
                        "Este pedido não possui débito para estornar."));

        if (lancamentoContaRepository.findByPedidoIdAndTipoLancamento(pedidoId, TipoLancamento.CREDITO)
                .isPresent()) {
            throw new IllegalStateException("O débito deste pedido já foi estornado");
        }

        Cliente cliente = debito.getCliente();

        // TODO: analisar possibilidade de credito positivo para o cliente caso o
        // pagamento estorne e devolva saldo positivo de credito
        if (debito.getValor().compareTo(cliente.calcularSaldoDevedor()) > 0) {
            throw new IllegalStateException(
                    "O estorno geraria saldo a favor do cliente. "
                            + "É necessário definir o tratamento desse valor.");
        }

        LancamentoConta estorno = new LancamentoConta(
                cliente,
                pedidoCancelado,
                TipoLancamento.CREDITO,
                debito.getValor(),
                "Estorno do débito referente ao pedido com o id " + pedidoId);

        LancamentoConta estornoSalvo = lancamentoContaRepository.save(estorno);

        cliente.getLancamentos().add(estornoSalvo);

        return toResponse(estornoSalvo);
    }

    public LancamentoContaResponse buscarPorId(Long lancamentoId) {
        return toResponse(buscarEntidadePorId(lancamentoId));
    }

    @Transactional(readOnly = true)
    public ContaClienteResponse consultarConta(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cliente com o id " + clienteId + " não encontrado."));

        return new ContaClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getLimiteCredito(),
                cliente.calcularSaldoDevedor(),
                cliente.calcularCreditoDisponivel());
    }

    @Transactional(readOnly = true)
    public Page<LancamentoContaResponse> listarPorCliente(
            Long clienteId,
            Pageable pageable,
            TipoLancamento tipoLancamento,
            LocalDate dataInicial,
            LocalDate dataFinal) {

        if (clienteId == null) {
            throw new IllegalArgumentException("O cliente é obrigatório para consultar o extrato.");
        }

        if (!clienteRepository.existsById(clienteId)) {
            throw new ClienteNaoEncontradoException(clienteId);
        }

        if (dataInicial != null
                && dataFinal != null
                && dataInicial.isAfter(dataFinal)) {
            throw new PeriodoInvalidoException();
        }

        Specification<LancamentoConta> filtros = LancamentoContaSpecification.comCliente(clienteId)
                .and(LancamentoContaSpecification.comTipo(tipoLancamento))
                .and(LancamentoContaSpecification.comDataInicial(dataInicial))
                .and(LancamentoContaSpecification.comDataFinal(dataFinal));

        return lancamentoContaRepository.findAll(filtros, pageable)
                .map(this::toResponse);
    }

    private LancamentoConta buscarEntidadePorId(Long id) {
        return lancamentoContaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Lancamento Conta com o id " + id + " não encontrado."));
    }

    private LancamentoContaResponse toResponse(LancamentoConta lancamentoConta) {
        return new LancamentoContaResponse(
                lancamentoConta.getId(),
                lancamentoConta.getCliente().getId(),
                lancamentoConta.getPedido() != null ? lancamentoConta.getPedido().getId() : null,
                lancamentoConta.getTipoLancamento(),
                lancamentoConta.getValor(),
                lancamentoConta.getDataHora(),
                lancamentoConta.getDescricao());
    }
}
