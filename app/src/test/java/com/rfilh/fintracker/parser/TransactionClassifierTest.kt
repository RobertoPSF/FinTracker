package com.rfilh.fintracker.parser

import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class TransactionClassifierTest {

    private lateinit var classifier: TransactionClassifier

    @Before
    fun setUp() {
        // working dir do teste unitário = diretório do módulo :app
        val rulesFile = File("src/main/assets/parser_rules.json")
        assertTrue("parser_rules.json não encontrado", rulesFile.exists())
        classifier = TransactionClassifier(ParserRules.fromJson(rulesFile.readText()))
    }

    private fun parsed(pkg: String, title: String, body: String): ParseResult.Parsed {
        val result = classifier.classify(pkg, title, body)
        assertTrue("esperava Parsed, veio $result", result is ParseResult.Parsed)
        return result as ParseResult.Parsed
    }

    @Test
    fun `compra no cartao vira despesa com valor e estabelecimento`() {
        val r = parsed("com.nu.production", "Nubank", "Compra aprovada: R$ 34,90 no MERCADO SAO JOSE")
        assertEquals(TransactionType.EXPENSE, r.type)
        assertEquals(3490L, r.amountCents)
        assertEquals("MERCADO SAO JOSE", r.counterparty)
        assertEquals(Confidence.HIGH, r.confidence)
    }

    @Test
    fun `pix recebido vira entrada`() {
        val r = parsed("com.nu.production", "Nubank", "Você recebeu um Pix de R$ 150,00 de João Silva")
        assertEquals(TransactionType.INCOME, r.type)
        assertEquals(15000L, r.amountCents)
        assertEquals("João Silva", r.counterparty)
    }

    @Test
    fun `pix enviado vira saida`() {
        val r = parsed("com.nu.production", "Nubank", "Você enviou um Pix de R$ 50,00 para Maria Souza")
        assertEquals(TransactionType.EXPENSE, r.type)
        assertEquals(5000L, r.amountCents)
        assertEquals("Maria Souza", r.counterparty)
    }

    @Test
    fun `valor com separador de milhar`() {
        val r = parsed("br.com.intermedium", "Inter", "Pix recebido de R$ 1.500,00 de EMPRESA XPTO LTDA")
        assertEquals(TransactionType.INCOME, r.type)
        assertEquals(150000L, r.amountCents)
        assertEquals("EMPRESA XPTO LTDA", r.counterparty)
    }

    @Test
    fun `pagamento com preposicao para`() {
        val r = parsed("com.picpay", "PicPay", "Você pagou R$ 89,90 para NETFLIX")
        assertEquals(TransactionType.EXPENSE, r.type)
        assertEquals(8990L, r.amountCents)
        assertEquals("NETFLIX", r.counterparty)
    }

    @Test
    fun `transferencia recebida em banco tradicional`() {
        val r = parsed("br.com.bb.android", "BB", "Transferência recebida de R$ 300,00 de PEDRO")
        assertEquals(TransactionType.INCOME, r.type)
        assertEquals(30000L, r.amountCents)
    }

    @Test
    fun `pix recebido no BB com texto real do app`() {
        val r = parsed(
            "br.com.bb.android",
            "Pix Recebido",
            "Que ótimo! Roberto P Santos Fh, CPF/CNPJ ***.746.614-** , enviou um Pix " +
                "de R\$ 0,01, da Instituição MERCADO PAGO IP LTDA, para sua conta Corrente às 11:52."
        )
        assertEquals(TransactionType.INCOME, r.type)
        assertEquals(1L, r.amountCents)
        assertEquals(Confidence.HIGH, r.confidence)
        assertEquals("Roberto P Santos Fh", r.counterparty)
    }

    @Test
    fun `pix enviado no BB com texto real do app`() {
        val r = parsed(
            "br.com.bb.android",
            "Pix Enviado",
            "Pix de R\$ 150,00 enviado da conta Corrente para Maria Souza, " +
                "Instituição Nu Pagamentos S.A., às 14:30."
        )
        assertEquals(TransactionType.EXPENSE, r.type)
        assertEquals(15000L, r.amountCents)
        assertEquals(Confidence.HIGH, r.confidence)
        assertEquals("Maria Souza", r.counterparty)
    }

    @Test
    fun `categoriza por keyword de estabelecimento`() {
        val food = parsed("com.nu.production", "Nubank", "Compra aprovada: R$ 45,00 no IFOOD")
        assertEquals("Alimentação", food.category)

        val subscription = parsed("com.picpay", "PicPay", "Você pagou R$ 39,90 para NETFLIX")
        assertEquals("Assinaturas", subscription.category)

        val uncategorized = parsed("com.nu.production", "Nubank", "Compra aprovada: R$ 10,00 no ACME")
        assertEquals(null, uncategorized.category)
    }

    @Test
    fun `compra com preposicao em`() {
        val r = parsed("com.c6bank.app", "C6", "Compra aprovada de R$ 12,00 em UBER TRIP")
        assertEquals(TransactionType.EXPENSE, r.type)
        assertEquals(1200L, r.amountCents)
        assertEquals("UBER TRIP", r.counterparty)
    }

    @Test
    fun `fatura e saldo sao ignorados`() {
        assertEquals(
            ParseResult.Ignored,
            classifier.classify("com.nu.production", "Nubank", "A fatura do seu cartão fechou: R$ 1.234,56")
        )
        assertEquals(
            ParseResult.Ignored,
            classifier.classify("com.itau", "Itaú", "Seu saldo disponível é de R$ 2.000,00")
        )
    }

    @Test
    fun `marketing sem valor e ignorado`() {
        assertEquals(
            ParseResult.Ignored,
            classifier.classify("com.mercadopago.wallet", "Mercado Pago", "Aproveite o cupom de desconto")
        )
    }

    @Test
    fun `banco desconhecido reduz confianca`() {
        val r = parsed("com.banco.inexistente", "Banco X", "Você recebeu R$ 10,00 de ANA")
        assertEquals(TransactionType.INCOME, r.type)
        assertEquals(Confidence.MEDIUM, r.confidence)
    }

    @Test
    fun `fintech nunca cadastrada ainda assim classifica corretamente pelas regras genericas`() {
        // Sem nenhuma entrada em parser_rules.json para esse pacote/fraseado — cobre o caso de
        // um banco/fintech que passa a existir depois do app já lançado.
        val r = parsed(
            "com.novafintech.app",
            "Pix realizado",
            "Pix realizado com sucesso no valor de R\$ 75,50 para João Pereira às 09:15."
        )
        assertEquals(TransactionType.EXPENSE, r.type)
        assertEquals(7550L, r.amountCents)
        assertEquals("João Pereira", r.counterparty)
        assertEquals(Confidence.MEDIUM, r.confidence)
    }

    @Test
    fun `pix agendado ou recusado nao vira transacao mesmo com valor`() {
        assertEquals(
            ParseResult.Ignored,
            classifier.classify("com.novafintech.app", "Pix agendado", "Pix de R\$ 200,00 agendado para amanhã às 09:00.")
        )
        assertEquals(
            ParseResult.Ignored,
            classifier.classify("com.novafintech.app", "Pix recusado", "Pix de R\$ 80,00 recusado pelo destinatário.")
        )
    }

    @Test
    fun `mensagem nao transacional nao e capturada por engano`() {
        // "enviado"/"compra" aparecem aqui, mas não como confirmação de pagamento — cobre o
        // risco de um stem genérico demais (ex. "envi", "compra") virar falso positivo.
        assertEquals(
            ParseResult.Ignored,
            classifier.classify("com.novafintech.app", "Comprovante disponível", "Seu comprovante foi enviado para o seu e-mail.")
        )
    }

    @Test
    fun `appLabel do Android e usado como nome quando nao ha override cadastrado`() {
        val result = classifier.classify(
            "com.novafintech.app", "Pix recebido", "Você recebeu um Pix de R\$ 80,00 de Carla", appLabel = "Nova Fintech"
        )
        assertTrue(result is ParseResult.Parsed)
        assertEquals("Nova Fintech", (result as ParseResult.Parsed).bankName)
    }

    @Test
    fun `stem generico credita reconhece fraseado nunca cadastrado`() {
        val r = parsed("com.outrafintech.app", "Notificação", "Você recebeu um crédito de R\$ 80,00 de Carla Dias")
        assertEquals(TransactionType.INCOME, r.type)
        assertEquals(8000L, r.amountCents)
    }

    @Test
    fun `compra no cartao sem estabelecimento extrai modalidade em vez de contraparte lixo`() {
        val r = parsed(
            "com.mercadopago.wallet",
            "Compra aprovada",
            "Foi feita uma compra de R\$ 49,50 no seu Cartão Mercado Pago Crédito."
        )
        assertEquals(TransactionType.EXPENSE, r.type)
        assertEquals(4950L, r.amountCents)
        assertEquals("Crédito", r.cardType)
        // "seu Cartão Mercado Pago Crédito" não é uma contraparte de verdade.
        assertEquals(null, r.counterparty)
    }

    @Test
    fun `modalidade do cartao reconhecida tambem na ordem invertida`() {
        val r = parsed("com.c6bank.app", "C6", "Compra aprovada de R\$ 20,00 em débito no cartão")
        assertEquals("Débito", r.cardType)
    }

    @Test
    fun `contraparte nunca e a propria conta do usuario, mesmo com mais de uma palavra antes de conta`() {
        val r = parsed(
            "com.mercadopago.wallet",
            "Pix recebido",
            "Você recebeu um Pix de R\$ 0,01. O valor foi creditado para a sua conta."
        )
        assertEquals(TransactionType.INCOME, r.type)
        assertEquals(null, r.counterparty)
    }

    @Test
    fun `parse de valores brl`() {
        assertEquals(123456L, Money.parseBrlToCents("1.234,56"))
        assertEquals(500L, Money.parseBrlToCents("R$ 5,00"))
        assertEquals(1000L, Money.parseBrlToCents("10"))
        assertNotNull(Money.parseBrlToCents("0,01"))
    }
}
