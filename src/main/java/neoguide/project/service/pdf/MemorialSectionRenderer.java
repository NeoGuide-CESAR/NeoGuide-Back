package neoguide.project.service.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import neoguide.project.model.CommercialUnit;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.SpecialLoads;

@Component
public class MemorialSectionRenderer {

    // Paleta Institucional NeoGuide / Concessionária
    private static final Color COLOR_PRIMARY = new Color(11, 60, 93);    // #0B3C5D
    private static final Color COLOR_TEXT = new Color(50, 50, 50);       // #323232
    private static final Color COLOR_ZEBRA = new Color(245, 247, 250);   // #F5F7FA
    private static final Color COLOR_BORDER = new Color(218, 224, 233);  // #DAE0E9
    private static final Color COLOR_SUCCESS = new Color(46, 125, 50);   // #2E7D32
    private static final Color COLOR_SUCCESS_BG = new Color(232, 245, 233);

    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, COLOR_TEXT);
    private static final Font FONT_SEC_HEADER = new Font(Font.HELVETICA, 9.5f, Font.BOLD, Color.WHITE);
    private static final Font FONT_TH = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font FONT_TD = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_TEXT);
    private static final Font FONT_TD_BOLD = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_TEXT);
    private static final Font FONT_NOTE = new Font(Font.HELVETICA, 7.5f, Font.ITALIC, COLOR_TEXT);
    private static final Font FONT_SEAL = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_SUCCESS);

    public void renderSection1Header(Document document, Project project) throws DocumentException {
        Paragraph pTitle = new Paragraph("MEMORIAL DESCRITIVO DE CÁLCULO DA DEMANDA DE ENERGIA ELÉTRICA", FONT_TITLE);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        pTitle.setSpacingAfter(2f);
        document.add(pTitle);

        Paragraph pNorms = new Paragraph("Conforme Normas Técnicas DIS-NOR-053 (Rev. 06) e DIS-NOR-030 (Rev. 07) — Neoenergia Pernambuco", FONT_SUBTITLE);
        pNorms.setAlignment(Element.ALIGN_CENTER);
        pNorms.setSpacingAfter(10f);
        document.add(pNorms);

        addSectionBanner(document, "1. IDENTIFICAÇÃO DA OBRA E DO RESPONSÁVEL TÉCNICO");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{22f, 28f, 22f, 28f});
        table.setSpacingAfter(8f);

        String responsavelNome = (project.getResponsavel() != null && !project.getResponsavel().isBlank())
                ? project.getResponsavel()
                : (project.getUser() != null ? project.getUser().getName() : "Não informado");

        String crea = (project.getCrea() != null && !project.getCrea().isBlank())
                ? project.getCrea()
                : (project.getUser() != null && project.getUser().getCrea() != null ? project.getUser().getCrea() : "Não informado");

        String enderecoCompleto = (project.getEndereco() != null ? project.getEndereco() : "")
                + (project.getBairro() != null ? " - " + project.getBairro() : "")
                + (project.getMunicipio() != null ? ", " + project.getMunicipio() : " - PE")
                + (project.getCep() != null ? " (CEP: " + project.getCep() + ")" : "");

        addKeyValue(table, "Empreendimento:", project.getTitulo(), false);
        addKeyValue(table, "Protocolo NeoGuide:", project.getProtocolo(), false);
        addKeyValue(table, "Endereço da Obra:", enderecoCompleto, false, 3);
        addKeyValue(table, "Responsável Técnico:", responsavelNome, true);
        addKeyValue(table, "Registro Profissional:", crea + " (CREA/CFT-PE)", true);
        addKeyValue(table, "ART / TRT de Projeto:", "Vinculada Eletronicamente ao CREA-PE", false);
        addKeyValue(table, "Data de Emissão:", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false);

        document.add(table);
    }

    public void renderSection2Supply(Document document, Project project, DemandResults results) throws DocumentException {
        addSectionBanner(document, "2. CARACTERÍSTICAS GERAIS DO FORNECIMENTO");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25f, 25f, 25f, 25f});
        table.setSpacingAfter(8f);

        String tensao = project.getTensaoFornecimento() != null ? project.getTensaoFornecimento() : "380_220V";
        String tensaoFormatada = tensao.contains("220") && !tensao.contains("380") ? "220/127 V (Trifásico a 4 fios)" : "380/220 V (Trifásico a 4 fios)";
        String nivelAtendimento = results.getDemandaGlobalKva().compareTo(new BigDecimal("300.00")) <= 0
                ? "Baixa Tensão Secundária (Rede de Distribuição)"
                : "Média Tensão Primária 13,8 kV (Subestação Própria)";

        addKeyValue(table, "Tensão Nominal:", tensaoFormatada, false);
        addKeyValue(table, "Frequência:", "60 Hz", false);
        addKeyValue(table, "Nível de Atendimento:", nivelAtendimento, false, 3);
        addKeyValue(table, "Padrão de Entrada:", "Edificação Coletiva MUC (DIS-NOR-053)", false, 3);

        document.add(table);
    }

    public void renderSection3ResidentialQ1(Document document, Project project, DemandResults results,
                                            List<ResidentialTypology> typologies) throws DocumentException {
        addSectionBanner(document, "3. QUADRO 1 — CARGA E DEMANDA RESIDENCIAL (Drf)");

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35f, 15f, 15f, 15f, 20f});
        table.setSpacingAfter(4f);

        addTableHeader(table, new String[]{"Tipologia Habitacional", "Área Útil", "Qtd (N)", "Smart/Studio", "Demanda Parcial"});

        if (typologies != null && !typologies.isEmpty()) {
            boolean zebra = false;
            for (ResidentialTypology t : typologies) {
                addCell(table, t.getNome(), zebra);
                addCell(table, t.getAreaUtilM2() + " m²", zebra, Element.ALIGN_RIGHT);
                addCell(table, String.valueOf(t.getQuantidadeUnidades()), zebra, Element.ALIGN_CENTER);
                addCell(table, t.isSmartStudio() ? "Sim" : "Não", zebra, Element.ALIGN_CENTER);
                addCell(table, "Conforme Q35/36", zebra, Element.ALIGN_RIGHT);
                zebra = !zebra;
            }
        } else {
            addCell(table, "Nenhuma tipologia residencial cadastrada", false, Element.ALIGN_LEFT, 5);
        }

        document.add(table);

        Paragraph pTotal = new Paragraph("Demanda Residencial Final Calculada (Drf): "
                + formatKva(results.getDemandaResidencialKva())
                + "  (Fatores Fc e Fr aplicados conf. Quadros 36 e 37)", FONT_TD_BOLD);
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        pTotal.setSpacingAfter(8f);
        document.add(pTotal);
    }

    public void renderSection4CommonAreasQ2(Document document, Project project, DemandResults results,
                                           List<CommonAreaLoad> commonLoads) throws DocumentException {
        addSectionBanner(document, "4. QUADRO 2 — SERVIÇOS E ÁREAS COMUNS (Ds)");

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30f, 25f, 15f, 15f, 15f});
        table.setSpacingAfter(4f);

        addTableHeader(table, new String[]{"Equipamento / Descrição", "Submódulo / Parcela", "Qtd", "Potência Unit.", "Demanda"});

        if (commonLoads != null && !commonLoads.isEmpty()) {
            boolean zebra = false;
            for (CommonAreaLoad load : commonLoads) {
                addCell(table, load.getTipoEquipamento() != null ? load.getTipoEquipamento() : "Carga Comum", zebra);
                addCell(table, load.getSubmodulo() != null ? load.getSubmodulo() : "ÁREA_COMUM", zebra);
                addCell(table, String.valueOf(load.getQuantidade() != null ? load.getQuantidade() : 1), zebra, Element.ALIGN_CENTER);
                addCell(table, (load.getPotenciaNominalKw() != null ? load.getPotenciaNominalKw() : BigDecimal.ZERO) + " kW", zebra, Element.ALIGN_RIGHT);
                addCell(table, formatKva(load.getDemandaCalculadaKva()), zebra, Element.ALIGN_RIGHT);
                zebra = !zebra;
            }
        } else {
            addCell(table, "Sem cargas condominiais cadastradas (Ds = 0,00 kVA)", false, Element.ALIGN_LEFT, 5);
        }

        document.add(table);

        Paragraph pTotal = new Paragraph("Demanda Total de Serviços e Áreas Comuns (Ds): "
                + formatKva(results.getDemandaServicoKva())
                + "  (DIS-NOR-030 Tabelas 7 a 22)", FONT_TD_BOLD);
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        pTotal.setSpacingAfter(8f);
        document.add(pTotal);
    }

    public void renderSection5SpecialCommercialQ3(Document document, Project project, DemandResults results,
                                                  List<CommercialUnit> commercialUnits,
                                                  SpecialLoads specialLoads) throws DocumentException {
        addSectionBanner(document, "5. QUADRO 3 — CARGAS ESPECIAIS E COMERCIAIS (Dve, Dmbi, Dc)");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35f, 25f, 20f, 20f});
        table.setSpacingAfter(4f);

        addTableHeader(table, new String[]{"Categoria de Carga", "Parâmetro Declarado", "Fator / Critério", "Demanda (kVA)"});

        // VE
        int vagasVe = specialLoads != null && specialLoads.getQtdPontosVe() != null ? specialLoads.getQtdPontosVe() : 0;
        BigDecimal potVe = specialLoads != null && specialLoads.getPotenciaEstacaoVeKw() != null ? specialLoads.getPotenciaEstacaoVeKw() : BigDecimal.ZERO;
        addCell(table, "Recarga de Veículos Elétricos (Dve)", false);
        addCell(table, vagasVe + " pontos (" + potVe + " kW/pt)", false);
        addCell(table, "Quadro 33 DIS-NOR-053", false);
        addCell(table, formatKva(results.getDemandaVeKva()), false, Element.ALIGN_RIGHT);

        // Motobomba de Incêndio
        BigDecimal potBomba = specialLoads != null && specialLoads.getPotenciaBombaIncendioCv() != null ? specialLoads.getPotenciaBombaIncendioCv() : BigDecimal.ZERO;
        addCell(table, "Motobomba de Combate a Incêndio (Dmbi)", true);
        addCell(table, potBomba + " CV", true);
        addCell(table, "Tabela 19 (Deriv. QDG)", true);
        addCell(table, formatKva(results.getDemandaIncendioKva()), true, Element.ALIGN_RIGHT);

        // Comercial
        int totalLojas = commercialUnits != null ? commercialUnits.size() : 0;
        addCell(table, "Unidades Comerciais e Lojas (Dc)", false);
        addCell(table, totalLojas + " unidade(s)", false);
        addCell(table, "Tabela 22 DIS-NOR-030", false);
        addCell(table, formatKva(results.getDemandaComercialKva()), false, Element.ALIGN_RIGHT);

        document.add(table);

        Paragraph pNote = new Paragraph("Nota Regulamentar: A motobomba de incêndio (" + formatKva(results.getDemandaIncendioKva())
                + ") deriva a montante da chave geral do QDG, não solicitando seus barramentos (DIS-NOR-053 Item 6.2).", FONT_NOTE);
        pNote.setSpacingAfter(8f);
        document.add(pNote);
    }

    public void renderSection6SynthesisEntranceQ4(Document document, Project project, DemandResults results) throws DocumentException {
        addSectionBanner(document, "6. QUADRO 4 — SÍNTESE DAS DEMANDAS E PADRÃO DE ENTRADA");

        // Síntese
        PdfPTable tableDed = new PdfPTable(2);
        tableDed.setWidthPercentage(100);
        tableDed.setWidths(new float[]{60f, 40f});
        tableDed.setSpacingAfter(6f);

        addKeyValue(tableDed, "Demanda Residencial Final (Drf):", formatKva(results.getDemandaResidencialKva()), false);
        addKeyValue(tableDed, "Demanda de Serviços (Ds):", formatKva(results.getDemandaServicoKva()), false);
        addKeyValue(tableDed, "Demanda Comercial (Dc):", formatKva(results.getDemandaComercialKva()), true);
        addKeyValue(tableDed, "Demanda Recarga Veicular (Dve):", formatKva(results.getDemandaVeKva()), true);
        addKeyValue(tableDed, "Demanda Motobomba de Incêndio (Dmbi):", formatKva(results.getDemandaIncendioKva()), false);
        addKeyValue(tableDed, "DEMANDA TOTAL DA EDIFICAÇÃO (Ded):", formatKva(results.getDemandaGlobalKva()) + " [Drf + Ds + Dc + Dve + Dmbi]", true);
        addKeyValue(tableDed, "DEMANDA DO BARRAMENTO GERAL (D_QDG):", formatKva(results.getDemandaQdgKva()) + " [Ded - Dmbi]", true);

        document.add(tableDed);

        // Padrão de Entrada
        PdfPTable tablePdr = new PdfPTable(3);
        tablePdr.setWidthPercentage(100);
        tablePdr.setWidths(new float[]{35f, 40f, 25f});
        tablePdr.setSpacingAfter(8f);

        addTableHeader(tablePdr, new String[]{"Componente de Entrada", "Especificação Dimensionada", "Referência"});

        addCell(tablePdr, "Disjuntor Termomagnético Geral", false);
        addCell(tablePdr, (results.getDisjuntorGeralA() != null ? results.getDisjuntorGeralA() : 0) + " A", false);
        addCell(tablePdr, "DIS-NOR-053 Tabela 2", false);

        addCell(tablePdr, "Condutor de Fase (Cobre XLPE 90ºC)", true);
        addCell(tablePdr, results.getCaboFaseMm2() != null ? results.getCaboFaseMm2() : "N/A", true);
        addCell(tablePdr, "DIS-NOR-053 Tabela 2", true);

        addCell(tablePdr, "Condutor de Neutro", false);
        addCell(tablePdr, results.getCaboNeutroMm2() != null ? results.getCaboNeutroMm2() : "N/A", false);
        addCell(tablePdr, "DIS-NOR-053 Tabela 2", false);

        addCell(tablePdr, "Barramento de Cobre Nu do QDG", true);
        addCell(tablePdr, results.getBarramentoCobre() != null ? results.getBarramentoCobre() : "N/A", true);
        addCell(tablePdr, "DIS-NOR-053 Tabela 3", true);

        addCell(tablePdr, "Capacidade Nominal do Barramento", false);
        addCell(tablePdr, (results.getCapacidadeBarramentoA() != null ? results.getCapacidadeBarramentoA() : 0) + " A", false);
        addCell(tablePdr, "DIS-NOR-053 Tabela 3", false);

        document.add(tablePdr);
    }

    public void renderSection7ComplianceSignature(Document document, Project project, DemandResults results) throws DocumentException {
        addSectionBanner(document, "7. AUDITORIA REGULATÓRIA E CHANCELA DE RESPONSABILIDADE");

        // Caixa do Selo de Conformidade
        PdfPTable sealTable = new PdfPTable(1);
        sealTable.setWidthPercentage(100);
        sealTable.setSpacingAfter(15f);

        PdfPCell sealCell = new PdfPCell();
        sealCell.setBackgroundColor(COLOR_SUCCESS_BG);
        sealCell.setBorderColor(COLOR_SUCCESS);
        sealCell.setBorderWidth(1.5f);
        sealCell.setPadding(8f);

        Paragraph pSeal = new Paragraph("CHANCELA DE AUDITORIA: CONFORME_DIS_NOR_053", FONT_SEAL);
        pSeal.setAlignment(Element.ALIGN_CENTER);
        sealCell.addElement(pSeal);

        Paragraph pSealDesc = new Paragraph(
                "O dimensionamento elétrico deste empreendimento foi validado eletronicamente contra as normas DIS-NOR-053 (Rev. 06), "
                        + "DIS-NOR-030 (Rev. 07) e RN ANEEL 1.000/2021, atendendo a todos os critérios de simultaneidade, ampacidade e segurança.",
                FONT_TD
        );
        pSealDesc.setAlignment(Element.ALIGN_CENTER);
        pSealDesc.setSpacingBefore(3f);
        sealCell.addElement(pSealDesc);

        sealTable.addCell(sealCell);
        document.add(sealTable);

        // Bloco de Assinatura Técnica
        String responsavel = (project.getResponsavel() != null && !project.getResponsavel().isBlank())
                ? project.getResponsavel()
                : (project.getUser() != null ? project.getUser().getName() : "Engenheiro Eletricista");

        String crea = (project.getCrea() != null && !project.getCrea().isBlank())
                ? project.getCrea()
                : (project.getUser() != null && project.getUser().getCrea() != null ? project.getUser().getCrea() : "CREA-PE");

        Paragraph pLine = new Paragraph("____________________________________________________________", FONT_TD_BOLD);
        pLine.setAlignment(Element.ALIGN_CENTER);
        pLine.setSpacingBefore(15f);
        document.add(pLine);

        Paragraph pSignName = new Paragraph(responsavel, FONT_TD_BOLD);
        pSignName.setAlignment(Element.ALIGN_CENTER);
        document.add(pSignName);

        Paragraph pSignCrea = new Paragraph("Registro Profissional: " + crea + " | ART / TRT Eletrônica", FONT_TD);
        pSignCrea.setAlignment(Element.ALIGN_CENTER);
        document.add(pSignCrea);

        Paragraph pSignLoc = new Paragraph("Recife - PE, " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")), FONT_NOTE);
        pSignLoc.setAlignment(Element.ALIGN_CENTER);
        document.add(pSignLoc);
    }

    // =========================================================================
    // UTILITÁRIOS DE DIAGRAMAÇÃO
    // =========================================================================

    private void addSectionBanner(Document document, String title) throws DocumentException {
        PdfPTable banner = new PdfPTable(1);
        banner.setWidthPercentage(100);
        banner.setSpacingBefore(4f);
        banner.setSpacingAfter(4f);

        PdfPCell cell = new PdfPCell(new Phrase(title, FONT_SEC_HEADER));
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setPadding(4.5f);
        cell.setBorder(PdfPCell.NO_BORDER);
        banner.addCell(cell);

        document.add(banner);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_TH));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(4f);
            cell.setBorderColor(COLOR_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addCell(PdfPTable table, String text, boolean zebra) {
        addCell(table, text, zebra, Element.ALIGN_LEFT, 1);
    }

    private void addCell(PdfPTable table, String text, boolean zebra, int alignment) {
        addCell(table, text, zebra, alignment, 1);
    }

    private void addCell(PdfPTable table, String text, boolean zebra, int alignment, int colSpan) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", FONT_TD));
        if (zebra) cell.setBackgroundColor(COLOR_ZEBRA);
        cell.setPadding(3.5f);
        cell.setBorderColor(COLOR_BORDER);
        cell.setHorizontalAlignment(alignment);
        if (colSpan > 1) cell.setColspan(colSpan);
        table.addCell(cell);
    }

    private void addKeyValue(PdfPTable table, String key, String value, boolean zebra) {
        addKeyValue(table, key, value, zebra, 1);
    }

    private void addKeyValue(PdfPTable table, String key, String value, boolean zebra, int valColSpan) {
        PdfPCell kCell = new PdfPCell(new Phrase(key, FONT_TD_BOLD));
        if (zebra) kCell.setBackgroundColor(COLOR_ZEBRA);
        kCell.setPadding(3.5f);
        kCell.setBorderColor(COLOR_BORDER);
        table.addCell(kCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value != null ? value : "—", FONT_TD));
        if (zebra) vCell.setBackgroundColor(COLOR_ZEBRA);
        vCell.setPadding(3.5f);
        vCell.setBorderColor(COLOR_BORDER);
        if (valColSpan > 1) vCell.setColspan(valColSpan);
        table.addCell(vCell);
    }

    private String formatKva(BigDecimal val) {
        if (val == null) return "0,00 kVA";
        return val.setScale(2, RoundingMode.HALF_UP).toString().replace('.', ',') + " kVA";
    }
}
