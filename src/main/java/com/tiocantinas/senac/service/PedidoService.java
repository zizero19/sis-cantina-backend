package com.tiocantinas.senac.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiocantinas.senac.dto.pedido.AdicionarItemPedidoRequest;
import com.tiocantinas.senac.dto.pedido.PedidoRequest;
import com.tiocantinas.senac.dto.pedido.PedidoResponse;
import com.tiocantinas.senac.exception.PedidoNaoEncontradoException;
import com.tiocantinas.senac.exception.ProdutoNaoEncontradoException;
import com.tiocantinas.senac.model.Cliente;
import com.tiocantinas.senac.model.ItemPedido;
import com.tiocantinas.senac.model.Pedido;
import com.tiocantinas.senac.model.Produto;
import com.tiocantinas.senac.model.enums.FormaPagamento;
import com.tiocantinas.senac.model.enums.StatusPedido;
import com.tiocantinas.senac.repository.ClienteRepository;
import com.tiocantinas.senac.repository.PedidoRepository;
import com.tiocantinas.senac.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        Pedido pedido = new Pedido(request.observacoes());

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        return toResponse(pedidoSalvo);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PedidoResponse adicionarItem(Long pedidoId, AdicionarItemPedidoRequest request) {
        Pedido pedido = buscarEntidadePorId(pedidoId);

        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new ProdutoNaoEncontradoException(request.produtoId()));

        ItemPedido itemPedido = new ItemPedido(produto, request.quantidade(), produto.getPreco());

        pedido.adicionarItem(itemPedido);

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse removerItem(Long pedidoId, Long itemId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        ItemPedido item = pedido.getItens()
                .stream()
                .filter(x -> x.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("O item não pertence a este pedido"));

        pedido.removerItem(item);

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse associarCliente(Long pedidoId, String cpf) {
        Pedido pedido = buscarEntidadePorId(pedidoId);

        Cliente cliente = clienteRepository.findByCpf(cpf).orElseThrow(() -> new IllegalArgumentException(
                "Cliente não encontrado."));

        pedido.associarCliente(cliente);

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse definirFormaPagamento(
            Long pedidoId,
            FormaPagamento formaPagamento) {

        Pedido pedido = buscarEntidadePorId(pedidoId);

        pedido.definirFormaPagamento(formaPagamento);

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse aguardarPagamento(Long pedidoId) {
        Pedido pedido = buscarEntidadePorId(pedidoId);

        pedido.aguardarPagamento();

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse finalizar(Long pedidoId) {
        Pedido pedido = buscarEntidadePorId(pedidoId);

        pedido.finalizarPedido();

        for (ItemPedido item : pedido.getItens()) {
            item.getProduto().baixarEstoque(item.getQuantidade());
        }

        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Long pedidoId) {
        Pedido pedido = buscarEntidadePorId(pedidoId);

        boolean estavaFinalizado = pedido.getStatusPedido() == StatusPedido.FINALIZADO;

        pedido.cancelarPedido();

        if (estavaFinalizado) {
            for (ItemPedido item : pedido.getItens()) {
                item.getProduto().reporEstoque(item.getQuantidade());
            }
        }

        return toResponse(pedido);
    }

    private PedidoResponse toResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getDataHora(),
                pedido.getStatusPedido(),
                pedido.getObservacoes(),
                pedido.getPrecoTotal(),
                pedido.getFormaPagamento(),
                pedido.getCliente() != null
                        ? pedido.getCliente().getId()
                        : null,
                pedido.getCaixa() != null
                        ? pedido.getCaixa().getId()
                        : null);
    }

    private Pedido buscarEntidadePorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }
}
