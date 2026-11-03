package com.tiocantinas.senac.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiocantinas.senac.dto.lancamento_conta.LancamentoContaResponse;
import com.tiocantinas.senac.exception.ClienteNaoEncontradoException;
import com.tiocantinas.senac.exception.PedidoNaoEncontradoException;
import com.tiocantinas.senac.model.Cliente;
import com.tiocantinas.senac.model.LancamentoConta;
import com.tiocantinas.senac.model.Pedido;
import com.tiocantinas.senac.model.enums.FormaPagamento;
import com.tiocantinas.senac.model.enums.StatusPedido;
import com.tiocantinas.senac.model.enums.TipoLancamento;
import com.tiocantinas.senac.repository.ClienteRepository;
import com.tiocantinas.senac.repository.LancamentoContaRepository;
import com.tiocantinas.senac.repository.PedidoRepository;

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
                    "Cliente com o id " + cliente.getId() + " não possui limite de crédito suficiente!");
        }

        if (cliente.getLancamentos().stream()
                .anyMatch(x -> x.getTipoLancamento() == TipoLancamento.DEBITO
                        && x.getPedido() != null
                        && x.getPedido().getId().equals(pedidoId))) {
            throw new IllegalStateException(
                    "Cliente com o id " + cliente.getId() + " já possui um lançamento de débito para o pedido com o id "
                            + pedidoId + "!");
        }

        LancamentoConta lancamento = new LancamentoConta(
                cliente,
                pedido,
                TipoLancamento.DEBITO,
                pedido.getPrecoTotal(),
                "Débito referente ao pedido com o id " + pedidoId);

        LancamentoConta lancamentoSalvo = lancamentoContaRepository.save(lancamento);

        cliente.getLancamentos().add(lancamentoSalvo);

        return toResponse(lancamentoSalvo);
    }

    // public LancamentoContaResponse registrarPagamento(Long clienteId,
    // RegistrarPagamentoRequest request);
    // public LancamentoContaResponse estornarDebitoDoPedido(Long pedidoId);
    // public LancamentoContaResponse buscarPorId(Long lancamentoId);
    // public ContaClienteResponse consultarConta(Long clienteId);
    // public Page<LancamentoContaResponse> listarPorCliente(Long clienteId,
    // Pageable pageable);

    private LancamentoConta buscarEntidadePorId(Long id) {
        return lancamentoContaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lancamento Conta com o id " + id + " não encontrado!"));
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
