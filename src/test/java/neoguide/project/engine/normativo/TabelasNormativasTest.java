package neoguide.project.engine.normativo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de regressão das tabelas normativas (DIS-NOR-053 Rev. 06 e DIS-NOR-030 Rev. 07).
 * Se um valor antigo (errado) voltar ao código, um destes testes deve falhar.
 */
class TabelasNormativasTest {

    @Test
    @DisplayName("Quadro 35: 39 faixas, do limite 40 m² até 1000 m² (sem faixas inventadas)")
    void quadro35_tamanhoEFaixas() {
        assertThat(TabelasNormativas.QUADRO_35).hasSize(39);
        assertThat(TabelasNormativas.QUADRO_35.get(0).maxM2()).isEqualByComparingTo("40");
        assertThat(TabelasNormativas.QUADRO_35.get(38).maxM2()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("Quadro 35: valores antigos errados não podem voltar (ex.: 61–70 m² = 1,65; até 40 m² = 1,20)")
    void quadro35_valoresAntigosNaoVoltam() {
        assertThat(TabelasNormativas.faixaArea(new BigDecimal("63")).demandaKva()).isEqualByComparingTo("1.47");
        assertThat(TabelasNormativas.faixaArea(new BigDecimal("68")).demandaKva()).isEqualByComparingTo("1.57");
        assertThat(TabelasNormativas.faixaArea(new BigDecimal("35")).demandaKva()).isEqualByComparingTo("1.00");
        assertThat(TabelasNormativas.faixaArea(new BigDecimal("35")).demandaKva()).isNotEqualByComparingTo("1.20");
    }

    @Test
    @DisplayName("Quadro 35: área acima de 1000 m² é rejeitada")
    void quadro35_acimaDoLimite() {
        assertThatThrownBy(() -> TabelasNormativas.faixaArea(new BigDecimal("1000.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Quadro 36: tabela discreta de 60 linhas, sem interpolação (N=20 = 87,20%; N=50 = 70,68%)")
    void quadro36_valoresDiscretos() {
        assertThat(TabelasNormativas.linhasQuadro36()).hasSize(60);
        assertThat(TabelasNormativas.fatorCoincidencia(8)).isEqualByComparingTo("0.9650");
        assertThat(TabelasNormativas.fatorCoincidencia(20)).isEqualByComparingTo("0.8720");
        assertThat(TabelasNormativas.fatorCoincidencia(50)).isEqualByComparingTo("0.7068");
        assertThat(TabelasNormativas.fatorCoincidencia(100)).isEqualByComparingTo("0.6823");
        // valores antigos que não podem voltar
        assertThat(TabelasNormativas.fatorCoincidencia(20)).isNotEqualByComparingTo("0.8560");
        assertThat(TabelasNormativas.fatorCoincidencia(50)).isNotEqualByComparingTo("0.7080");
    }

    @Test
    @DisplayName("Quadro 33 (VE): 1–10 → 1,00; 11–20 → 0,86; 21–30 → 0,80; 31–40 → 0,78; 41–50 → 0,75; 51–75 → 0,70; 76–100 → 0,65; >100 → 0,60")
    void quadro33_faixas() {
        assertThat(TabelasNormativas.fatorVe(1)).isEqualByComparingTo("1.00");
        assertThat(TabelasNormativas.fatorVe(10)).isEqualByComparingTo("1.00");
        assertThat(TabelasNormativas.fatorVe(11)).isEqualByComparingTo("0.86");
        assertThat(TabelasNormativas.fatorVe(20)).isEqualByComparingTo("0.86");
        assertThat(TabelasNormativas.fatorVe(30)).isEqualByComparingTo("0.80");
        assertThat(TabelasNormativas.fatorVe(40)).isEqualByComparingTo("0.78");
        assertThat(TabelasNormativas.fatorVe(50)).isEqualByComparingTo("0.75");
        assertThat(TabelasNormativas.fatorVe(75)).isEqualByComparingTo("0.70");
        assertThat(TabelasNormativas.fatorVe(100)).isEqualByComparingTo("0.65");
        assertThat(TabelasNormativas.fatorVe(101)).isEqualByComparingTo("0.60");
        // Regressão: 4 pontos não é 0,80 nem 8 pontos 0,70 (valores antigos)
        assertThat(TabelasNormativas.fatorVe(4)).isNotEqualByComparingTo("0.80");
    }

    @Test
    @DisplayName("Tabela 19 (DIS-NOR-030 Rev. 07): R9 — 1 cv = 1,52; 2 cv = 2,70; 5 cv = 6,02; 10 cv = 11,54 kVA")
    void tabela19_valoresDeReferencia() {
        assertThat(TabelasNormativas.kvaTabela19(new BigDecimal("1"))).isEqualByComparingTo("1.52");
        assertThat(TabelasNormativas.kvaTabela19(new BigDecimal("2"))).isEqualByComparingTo("2.70");
        assertThat(TabelasNormativas.kvaTabela19(new BigDecimal("5"))).isEqualByComparingTo("6.02");
        assertThat(TabelasNormativas.kvaTabela19(new BigDecimal("10"))).isEqualByComparingTo("11.54");
        // Regressão: 5 cv = 5,27 e 1/2 cv = 1180 VA são valores antigos
        assertThat(TabelasNormativas.kvaTabela19(new BigDecimal("5"))).isNotEqualByComparingTo("5.27");
        assertThat(TabelasNormativas.kvaTabela19(new BigDecimal("0.50"))).isEqualByComparingTo("0.87");
    }
}
