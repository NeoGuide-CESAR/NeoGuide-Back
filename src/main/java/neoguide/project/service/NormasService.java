package neoguide.project.service;

import neoguide.project.dto.normas.NormaResponse;
import neoguide.project.dto.normas.PremissaResponse;
import neoguide.project.dto.normas.TabelaNormaResponse;
import neoguide.project.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Normas, tabelas e premissas exibidas na página "Normas técnicas". Dados só de leitura.
 * As datas de aprovação foram lidas do cabeçalho de cada PDF; a página do PDF é a da tabela no documento.
 */
@Service
public class NormasService {

    static final String NOR_053 = "DIS-NOR-053";
    static final String NOR_030 = "DIS-NOR-030";
    private static final String CATALOGO = "/api/v1/catalogo/";

    private final String urlDisNor053;
    private final String urlDisNor030;

    public NormasService(@Value("${neoguide.normas.dis-nor-053.url:}") String urlDisNor053,
                         @Value("${neoguide.normas.dis-nor-030.url:}") String urlDisNor030) {
        this.urlDisNor053 = urlDisNor053;
        this.urlDisNor030 = urlDisNor030;
    }

    @Cacheable("normas")
    public List<NormaResponse> normas() {
        return List.of(
                new NormaResponse(NOR_053, "Fornecimento de Energia Elétrica a Edificações com Múltiplas Unidades Consumidoras até 34,5 kV",
                        "06", LocalDate.of(2025, 9, 9), "PRINCIPAL", url(urlDisNor053),
                        "Edificações com múltiplas unidades consumidoras"),
                new NormaResponse(NOR_030, "Fornecimento de Energia Elétrica em Tensão Secundária de Distribuição a Edificações Individuais",
                        "07", LocalDate.of(2026, 4, 17), "COMPLEMENTAR", url(urlDisNor030),
                        "Edificações individuais (método da carga instalada)"));
    }

    @Cacheable(value = "normasTabelas", key = "#codigo.toUpperCase()")
    public List<TabelaNormaResponse> tabelas(String codigo) {
        return switch (codigo.toUpperCase(Locale.ROOT)) {
            case NOR_053 -> tabelas053();
            case NOR_030 -> tabelas030();
            default -> throw new ResourceNotFoundException("Norma não encontrada: " + codigo);
        };
    }

    @Cacheable("normasPremissas")
    public List<PremissaResponse> premissas() {
        return PremissasNormativas.TODAS;
    }

    private static String url(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    private static List<TabelaNormaResponse> tabelas053() {
        String ref = NOR_053 + " · ";
        return List.of(
                t("quadro-35", "Quadro 35 · Demanda do apartamento em função da área útil", ref + "Anexo I · Quadro 35", 107, "faixas-area", List.of(2), false, List.of()),
                t("quadro-36", "Quadro 36 · Fator de coincidência pelo número de apartamentos", ref + "Anexo I · Quadro 36", 107, "fatores-coincidencia-residencial", List.of(2), false,
                        List.of("Edificações do tipo Smart, Studio ou semelhantes com mais de 15 unidades usam fator de coincidência de 90%.")),
                t("quadro-37", "Quadro 37 · Fator de segurança (Fr)", ref + "Anexo I · Quadro 37", 108, "fatores-seguranca-residencial", List.of(2), false,
                        List.of("Fator de segurança acima do recomendado exige justificativa.")),
                t("quadro-33", "Quadro 33 · Fator de demanda dos agrupamentos de recarga veicular", ref + "Quadro 33", 98, "normas/fatores-recarga-ve", List.of(5), false, List.of()),
                t("tabela-2", "Tabela 2 (Anexo II) · Entrada de serviço 380/220 V", ref + "Anexo II · Tabela 2", 123, "normas/dimensionamento-entrada/380v", List.of(), true,
                        List.of("Disjuntores a partir de 250 A têm relé de sobrecorrente ajustável, de 0,9 a 1,2 vezes a corrente nominal (nota 6).",
                                "Cada faixa admite uma seção de condutor acima da indicada, desde que se mantenha o disjuntor da faixa (nota 7).")),
                t("tabela-1", "Tabela 1 (Anexo II) · Entrada de serviço 220/127 V (informativa; não se aplica a Pernambuco)", ref + "Anexo II · Tabela 1", 123, "normas/dimensionamento-entrada/220v", List.of(), false, List.of()),
                t("tabela-3", "Tabela 3 (Anexo II) · Barramento de cobre do QDG", ref + "Anexo II · Tabela 3", 125, "normas/barramentos-qdg", List.of(), true, List.of()));
    }

    private static List<TabelaNormaResponse> tabelas030() {
        String ref = NOR_030 + " · ";
        return List.of(
                t("tabela-7", "Tabela 7 · Chuveiros, torneiras, aquecedores de passagem e ferros", ref + "Tabela 7", 66, "fatores-aquecedores-passagem", List.of(3), false, List.of()),
                t("tabela-8", "Tabela 8 · Aquecedor central ou de acumulação (boiler)", ref + "Tabela 8", 66, "fatores-boilers", List.of(3), false, List.of()),
                t("tabela-9", "Tabela 9 · Secadora, forno elétrico, lavadora, lava-louça e micro-ondas", ref + "Tabela 9", 66, "fatores-eletrodomesticos", List.of(3), false, List.of()),
                t("tabela-10", "Tabela 10 · Fogões elétricos", ref + "Tabela 10", 67, "fatores-fogao-cooktop", List.of(3), false, List.of()),
                t("tabela-11", "Tabela 11 · Condicionadores de ar", ref + "Tabela 11", 67, "aparelhos-climatizacao", List.of(3), false, List.of()),
                t("tabela-12", "Tabela 12 · Fatores de demanda para condicionadores de ar", ref + "Tabela 12", 68, "fatores-ar-condicionado", List.of(3), false, List.of()),
                t("tabela-13", "Tabela 13 · Estações de recarga individuais", ref + "Tabela 13", 68, null, List.of(), false, List.of()),
                t("tabela-14", "Tabela 14 · Motores", ref + "Tabela 14", 68, "fatores-motores", List.of(3), false, List.of()),
                t("tabela-15", "Tabela 15 · Equipamentos especiais", ref + "Tabela 15", 68, "fatores-equipamentos-especiais", List.of(3), false, List.of()),
                t("tabela-16", "Tabela 16 · Bombas e hidromassagem", ref + "Tabela 16", 68, "normas/fatores-bombas-piscina", List.of(3), false, List.of()),
                t("tabela-17", "Tabela 17 · Dispositivos de partida de motores", ref + "Tabela 17", 69, "normas/dispositivos-partida-motores", List.of(3), false, List.of()),
                t("tabela-18", "Tabela 18 · Motores monofásicos", ref + "Tabela 18", 69, "normas/motores-monofasicos", List.of(3), false, List.of()),
                t("tabela-19", "Tabela 19 · Motores trifásicos", ref + "Tabela 19", 70, "motores-padrao", List.of(3, 5), false, List.of()),
                t("tabela-22", "Tabela 22 · Carga mínima e fatores para iluminação e tomadas", ref + "Tabela 22", 72, "cargas-minimas-ocupacao", List.of(3, 4), false, List.of()));
    }

    private static TabelaNormaResponse t(String ancora, String titulo, String referencia, int pagina, String endpoint,
                                         List<Integer> etapas, boolean usadaNoResultado, List<String> notas) {
        return new TabelaNormaResponse(ancora, titulo, referencia, pagina, endpoint == null ? null : CATALOGO + endpoint,
                endpoint != null, etapas, usadaNoResultado, notas);
    }
}
