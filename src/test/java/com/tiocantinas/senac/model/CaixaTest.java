package com.tiocantinas.senac.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tiocantinas.senac.model.enums.FormaPagamento;
import com.tiocantinas.senac.model.enums.StatusCaixa;
import com.tiocantinas.senac.model.enums.StatusPedido;

class CaixaTest {

    @Test
    void deveAbrirCaixaComSaldoInicialETotaisZerados() {
        Caixa caixa = novoCaixa();

        assertEquals(StatusCaixa.ABERTO, caixa.getStatus());
        assertNotNull(caixa.getAbertura());
        assertNull(caixa.getFechamento());
        assertNull(caixa.getValorFechamento());
        assertTrue(caixa.getPedidos().isEmpty());
        assertTotais(caixa, "0", "0", "0", "0");
        assertValor("100", caixa.calcularValorEsperado());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "\t" })
    void deveRecusarAberturaSemResponsavel(String responsavel) {
        assertThrows(IllegalArgumentException.class,
                () -> new Caixa(responsavel, BigDecimal.ZERO, null));
    }

    @Test
    void deveRecusarAberturaComValorNuloOuNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Caixa("Operador", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Caixa("Operador", new BigDecimal("-0.01"), null));
    }

    @Test
    void devePermitirAberturaSemTroco() {
        Caixa caixa = new Caixa("Operador", BigDecimal.ZERO, null);

        assertValor("0", caixa.calcularValorEsperado());
    }

    @ParameterizedTest
    @EnumSource(FormaPagamento.class)
    void deveRegistrarVendaNoTotalCorrespondente(FormaPagamento forma) {
        Caixa caixa = novoCaixa();
        Pedido pedido = pedidoFinalizado(forma);

        caixa.registrarPedido(pedido);

        assertSame(caixa, pedido.getCaixa());
        assertEquals(1, caixa.getPedidos().size());
        assertSame(pedido, caixa.getPedidos().getFirst());
        assertTotais(caixa,
                forma == FormaPagamento.DINHEIRO ? "10" : "0",
                forma == FormaPagamento.DEBITO || forma == FormaPagamento.CREDITO ? "10" : "0",
                forma == FormaPagamento.PIX ? "10" : "0",
                forma == FormaPagamento.A_PRAZO ? "10" : "0");
    }

    @Test
    void deveSomarVendasEConsiderarSomenteDinheiroNaConferencia() {
        Caixa caixa = novoCaixa();
        for (FormaPagamento forma : FormaPagamento.values()) {
            caixa.registrarPedido(pedidoFinalizado(forma));
        }

        assertTotais(caixa, "10", "20", "10", "10");
        assertValor("110", caixa.calcularValorEsperado());
    }

    @Test
    void deveRecusarRegistroDuplicadoSemAlterarTotais() {
        Caixa caixa = novoCaixa();
        Pedido pedido = pedidoFinalizado(FormaPagamento.DINHEIRO);
        caixa.registrarPedido(pedido);

        assertThrows(IllegalStateException.class, () -> caixa.registrarPedido(pedido));

        assertEquals(1, caixa.getPedidos().size());
        assertTotais(caixa, "10", "0", "0", "0");
    }

    @Test
    void deveRecusarPedidoJaRegistradoEmOutroCaixa() {
        Caixa primeiro = novoCaixa();
        Caixa segundo = novoCaixa();
        Pedido pedido = pedidoFinalizado(FormaPagamento.PIX);
        primeiro.registrarPedido(pedido);

        assertThrows(IllegalStateException.class, () -> segundo.registrarPedido(pedido));

        assertSame(primeiro, pedido.getCaixa());
        assertTrue(segundo.getPedidos().isEmpty());
        assertTotais(primeiro, "0", "0", "10", "0");
        assertTotais(segundo, "0", "0", "0", "0");
    }

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = { "CRIADO", "AGUARDANDO_PAGAMENTO", "CANCELADO" })
    void deveRecusarPedidoNaoFinalizado(StatusPedido status) {
        Caixa caixa = novoCaixa();
        Pedido pedido = pedidoAguardandoPagamento(FormaPagamento.DINHEIRO);
        if (status == StatusPedido.CRIADO) {
            pedido = new Pedido("Ainda em criação");
        } else if (status == StatusPedido.CANCELADO) {
            pedido.cancelarPedido();
        }
        Pedido candidato = pedido;

        assertThrows(IllegalStateException.class, () -> caixa.registrarPedido(candidato));

        assertNull(pedido.getCaixa());
        assertTrue(caixa.getPedidos().isEmpty());
        assertTotais(caixa, "0", "0", "0", "0");
    }

    @Test
    void deveRecusarPedidoNulo() {
        Caixa caixa = novoCaixa();

        assertThrows(IllegalArgumentException.class, () -> caixa.registrarPedido(null));
        assertTotais(caixa, "0", "0", "0", "0");
    }

    @Test
    void deveRecusarRegistroEmCaixaFechado() {
        Caixa caixa = novoCaixa();
        caixa.fechar(new BigDecimal("100"));
        Pedido pedido = pedidoFinalizado(FormaPagamento.DINHEIRO);

        assertThrows(IllegalStateException.class, () -> caixa.registrarPedido(pedido));

        assertNull(pedido.getCaixa());
        assertTrue(caixa.getPedidos().isEmpty());
        assertTotais(caixa, "0", "0", "0", "0");
    }

    @ParameterizedTest
    @EnumSource(FormaPagamento.class)
    void deveEstornarSomenteVendaCanceladaEPreservarHistorico(FormaPagamento forma) {
        Caixa caixa = novoCaixa();
        Pedido mantido = pedidoFinalizado(forma);
        Pedido cancelado = pedidoFinalizado(forma);
        caixa.registrarPedido(mantido);
        caixa.registrarPedido(cancelado);

        cancelado.cancelarPedido();

        assertEquals(StatusPedido.CANCELADO, cancelado.getStatusPedido());
        assertEquals(StatusPedido.FINALIZADO, mantido.getStatusPedido());
        assertSame(caixa, cancelado.getCaixa());
        assertEquals(2, caixa.getPedidos().size());
        assertTotais(caixa,
                forma == FormaPagamento.DINHEIRO ? "10" : "0",
                forma == FormaPagamento.DEBITO || forma == FormaPagamento.CREDITO ? "10" : "0",
                forma == FormaPagamento.PIX ? "10" : "0",
                forma == FormaPagamento.A_PRAZO ? "10" : "0");

        assertThrows(IllegalStateException.class, cancelado::cancelarPedido);
        assertTotais(caixa,
                forma == FormaPagamento.DINHEIRO ? "10" : "0",
                forma == FormaPagamento.DEBITO || forma == FormaPagamento.CREDITO ? "10" : "0",
                forma == FormaPagamento.PIX ? "10" : "0",
                forma == FormaPagamento.A_PRAZO ? "10" : "0");
        assertValor(forma == FormaPagamento.DINHEIRO ? "110" : "100", caixa.calcularValorEsperado());
    }

    @Test
    void deveRecusarEstornoDePedidoDeOutroCaixa() {
        Caixa caixa = novoCaixa();
        Caixa outro = novoCaixa();
        Pedido pedido = pedidoFinalizado(FormaPagamento.PIX);
        outro.registrarPedido(pedido);

        assertThrows(IllegalArgumentException.class, () -> caixa.estornarPedido(pedido));

        assertEquals(StatusPedido.FINALIZADO, pedido.getStatusPedido());
        assertTotais(caixa, "0", "0", "0", "0");
        assertTotais(outro, "0", "0", "10", "0");
    }

    @Test
    void devePreservarPedidoETotaisAoTentarCancelarAposFechamento() {
        Caixa caixa = novoCaixa();
        Pedido pedido = pedidoFinalizado(FormaPagamento.DINHEIRO);
        caixa.registrarPedido(pedido);
        caixa.fechar(new BigDecimal("110"));

        assertThrows(IllegalStateException.class, pedido::cancelarPedido);

        assertEquals(StatusPedido.FINALIZADO, pedido.getStatusPedido());
        assertTotais(caixa, "10", "0", "0", "0");
        assertValor("0", caixa.calcularDiferenca());
    }

    @Test
    void devePermitirCancelarPedidoSemCaixa() {
        Pedido pedido = pedidoFinalizado(FormaPagamento.DINHEIRO);

        pedido.cancelarPedido();

        assertEquals(StatusPedido.CANCELADO, pedido.getStatusPedido());
    }

    @Test
    void deveImpedirAlteracaoDiretaDaColecaoDePedidos() {
        Caixa caixa = novoCaixa();
        Pedido pedido = pedidoFinalizado(FormaPagamento.DINHEIRO);
        caixa.registrarPedido(pedido);

        assertThrows(UnsupportedOperationException.class, () -> caixa.getPedidos().clear());
        assertEquals(1, caixa.getPedidos().size());
        assertSame(caixa, pedido.getCaixa());
    }

    @ParameterizedTest
    @ValueSource(strings = { "108", "110", "112" })
    void deveFecharCaixaECalcularFaltaOuSobra(String valorContado) {
        Caixa caixa = novoCaixa();
        caixa.registrarPedido(pedidoFinalizado(FormaPagamento.DINHEIRO));
        caixa.registrarPedido(pedidoFinalizado(FormaPagamento.PIX));
        BigDecimal contado = new BigDecimal(valorContado);

        caixa.fechar(contado);

        assertEquals(StatusCaixa.FECHADO, caixa.getStatus());
        assertNotNull(caixa.getFechamento());
        assertEquals(contado, caixa.getValorFechamento());
        assertValor("110", caixa.calcularValorEsperado());
        assertEquals(0, contado.subtract(new BigDecimal("110")).compareTo(caixa.calcularDiferenca()));
    }

    @Test
    void deveConferirDinheiroAposEstorno() {
        Caixa caixa = novoCaixa();
        Pedido pedido = pedidoFinalizado(FormaPagamento.DINHEIRO);
        caixa.registrarPedido(pedido);
        pedido.cancelarPedido();

        caixa.fechar(new BigDecimal("100"));

        assertValor("100", caixa.calcularValorEsperado());
        assertValor("0", caixa.calcularDiferenca());
    }

    @Test
    void deveRecusarFechamentoComValorNuloOuNegativoSemFecharCaixa() {
        Caixa caixa = novoCaixa();

        assertThrows(IllegalArgumentException.class, () -> caixa.fechar(null));
        assertThrows(IllegalArgumentException.class, () -> caixa.fechar(new BigDecimal("-1")));

        assertEquals(StatusCaixa.ABERTO, caixa.getStatus());
        assertNull(caixa.getFechamento());
        assertNull(caixa.getValorFechamento());
    }

    @Test
    void deveRecusarSegundoFechamentoEPreservarConferenciaOriginal() {
        Caixa caixa = novoCaixa();
        caixa.fechar(new BigDecimal("100"));
        var fechamentoOriginal = caixa.getFechamento();

        assertThrows(IllegalStateException.class, () -> caixa.fechar(new BigDecimal("200")));

        assertEquals(fechamentoOriginal, caixa.getFechamento());
        assertValor("100", caixa.getValorFechamento());
    }

    @Test
    void deveRecusarDiferencaAntesDoFechamento() {
        Caixa caixa = novoCaixa();

        assertThrows(IllegalStateException.class, caixa::calcularDiferenca);
    }

    private Caixa novoCaixa() {
        return new Caixa("Operador", new BigDecimal("100"), "Caixa de teste");
    }

    private Pedido pedidoFinalizado(FormaPagamento forma) {
        Pedido pedido = pedidoAguardandoPagamento(forma);
        pedido.finalizarPedido();
        return pedido;
    }

    private Pedido pedidoAguardandoPagamento(FormaPagamento forma) {
        Produto produto = new Produto();
        produto.setNome("Produto teste");
        produto.setPreco(new BigDecimal("5.00"));
        Pedido pedido = new Pedido("Venda de teste");
        pedido.adicionarItem(new ItemPedido(produto, new BigDecimal("2"), produto.getPreco()));
        pedido.definirFormaPagamento(forma);
        if (forma == FormaPagamento.A_PRAZO) {
            Cliente cliente = new Cliente();
            cliente.setLimiteCredito(new BigDecimal("100"));
            pedido.associarCliente(cliente);
        }
        pedido.aguardarPagamento();
        return pedido;
    }

    private void assertTotais(Caixa caixa, String dinheiro, String cartao, String pix, String prazo) {
        assertValor(dinheiro, caixa.getTotalDinheiro());
        assertValor(cartao, caixa.getTotalCartao());
        assertValor(pix, caixa.getTotalPix());
        assertValor(prazo, caixa.getTotalAPrazo());
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual));
    }
}
