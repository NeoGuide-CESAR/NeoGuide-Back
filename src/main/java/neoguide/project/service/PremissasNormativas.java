package neoguide.project.service;

import neoguide.project.dto.normas.PremissaResponse;

import java.util.List;

/**
 * Premissas do NeoGuide: pontos em que a norma deixa margem de interpretação ou não define um valor.
 * Fonte única da página "Normas técnicas" e do memorial em PDF.
 */
public final class PremissasNormativas {

    private PremissasNormativas() {
    }

    public static final List<PremissaResponse> TODAS = List.of(
            new PremissaResponse("PRE-FP",
                    "Fator de potência: iluminação LED 0,80; tomadas 1,00; chuveiros, boilers, fornos e fogões 1,00; "
                            + "eletrodomésticos 0,92; ar-condicionado 0,92; espaços comerciais 0,92; elevadores e portões 0,92 "
                            + "quando a placa não informa; bombas de piscina 1,00; recarga de VE 1,00 quando o fabricante não informa.",
                    "DIS-NOR-030 · item 6.27"),
            new PremissaResponse("PRE-COCCAO",
                    "Cooktops e fogões pela Tabela 10; fornos pela Tabela 9.",
                    "DIS-NOR-030 · Tabelas 9 e 10"),
            new PremissaResponse("PRE-ESCRITORIO",
                    "Escalonamento de 20 kW (fator 1,00) e excedente (fator 0,70) só para escritórios, como na Tabela 22; "
                            + "lojas com fator 1,00.",
                    "DIS-NOR-030 · Tabela 22"),
            new PremissaResponse("PRE-ENTRADA",
                    "Entrada de serviço selecionada pela demanda da edificação (Ded); barramento pela demanda do QDG (DQDG). "
                            + "Barramento maior que o mínimo é aceito, com justificativa.",
                    "DIS-NOR-053 · Anexo II · Tabelas 2 e 3"),
            new PremissaResponse("PRE-MISTO",
                    "Edificação residencial com espaços comerciais é tratada como mista no enquadramento (limite de baixa "
                            + "tensão de 225 kVA).",
                    "DIS-NOR-053 · itens 6.2.4 e 6.2.6"),
            new PremissaResponse("PRE-CARGA-MINIMA",
                    "Carga instalada comercial abaixo da carga mínima da Tabela 22 gera alerta, sem bloquear.",
                    "DIS-NOR-030 · Tabela 22"),
            new PremissaResponse("PRE-ARREDONDAMENTO",
                    "Cálculo sem arredondamento intermediário; tela com 1 casa decimal; memorial com 2.",
                    "Projeto NeoGuide")
    );
}
