package com.tiocantinas.senac.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiocantinas.senac.dto.caixa.AbrirCaixaRequest;
import com.tiocantinas.senac.dto.caixa.CaixaResponse;
import com.tiocantinas.senac.dto.caixa.FecharCaixaRequest;
import com.tiocantinas.senac.exception.CaixaJaAbertoException;
import com.tiocantinas.senac.exception.CaixaNaoEncontradoException;
import com.tiocantinas.senac.exception.PedidoNaoEncontradoException;
import com.tiocantinas.senac.model.Caixa;
import com.tiocantinas.senac.model.Pedido;
import com.tiocantinas.senac.model.enums.StatusCaixa;
import com.tiocantinas.senac.repository.CaixaRepository;
import com.tiocantinas.senac.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaixaService {

    private final CaixaRepository caixaRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public CaixaResponse abrir(AbrirCaixaRequest caixa) {
        if (caixaRepository.existsByStatus(StatusCaixa.ABERTO))
            throw new CaixaJaAbertoException();

        Caixa novoCaixa = new Caixa(
                caixa.responsavelAbertura(),
                caixa.valorAbertura(),
                caixa.observacoes());

        Caixa caixaSalvo = caixaRepository.save(novoCaixa);

        return toResponse(caixaSalvo);
    }

    @Transactional(readOnly = true)
    public CaixaResponse buscarPorId(Long id) {
        return toResponse(buscarCaixaPorEntidadeId(id));
    }

    @Transactional(readOnly = true)
    public List<CaixaResponse> listarTodos() {
        return caixaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CaixaResponse> buscarAberto() {
        return caixaRepository.findByStatus(StatusCaixa.ABERTO).map(this::toResponse);
    }

    @Transactional
    public CaixaResponse fechar(Long id, FecharCaixaRequest request) {
        Caixa caixaAberto = buscarCaixaPorEntidadeId(id);

        caixaAberto.fechar(request.valorFechamento());

        return toResponse(caixaAberto);
    }

    @Transactional
    public void registrarPedido(Long pedidoId, Long caixaId) {
        Pedido pedido = buscarPedidoPorEntidadeId(pedidoId);
        Caixa caixa = buscarCaixaPorEntidadeId(caixaId);

        caixa.registrarPedido(pedido);
    }

    @Transactional(readOnly = true)
    public Page<CaixaResponse> listarFechados(Pageable pageable) {
        return caixaRepository.findAllByStatus(StatusCaixa.FECHADO, pageable).map(this::toResponse);
    }

    private Caixa buscarCaixaPorEntidadeId(Long id) {
        return caixaRepository.findById(id)
                .orElseThrow(() -> new CaixaNaoEncontradoException(id));
    }

    private Pedido buscarPedidoPorEntidadeId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException(id));
    }

    private CaixaResponse toResponse(Caixa caixa) {
        return new CaixaResponse(
                caixa.getId(),
                caixa.getResponsavelAbertura(),
                caixa.getValorAbertura(),
                caixa.getValorFechamento(),
                caixa.getTotalDinheiro(),
                caixa.getTotalCartao(),
                caixa.getTotalPix(),
                caixa.getTotalAPrazo(),
                caixa.calcularValorEsperado(),
                caixa.getStatus() == StatusCaixa.FECHADO
                        ? caixa.calcularDiferenca()
                        : null,
                caixa.getStatus(),
                caixa.getAbertura(),
                caixa.getFechamento(),
                caixa.getObservacoes());
    }
}
