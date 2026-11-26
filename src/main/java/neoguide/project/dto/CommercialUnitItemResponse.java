package neoguide.project.dto;

import neoguide.project.model.CommercialUnit;

import java.math.BigDecimal;

/** Espaço comercial gravado, com o Dc calculado pelo back e o alerta de carga mínima (quando houver). */
public class CommercialUnitItemResponse {

    private Long id;
    private Integer ordem;
    private String ocupacaoCodigo;
    private String rotulo;
    private BigDecimal areaM2;
    private BigDecimal cargaInstaladaKw;
    private BigDecimal cargaMinimaKw;
    private BigDecimal demandaCalculadaKva;
    private String alerta;

    public CommercialUnitItemResponse() {
    }

    public CommercialUnitItemResponse(Long id, Integer ordem, String ocupacaoCodigo, String rotulo, BigDecimal areaM2,
                                      BigDecimal cargaInstaladaKw, BigDecimal cargaMinimaKw,
                                      BigDecimal demandaCalculadaKva, String alerta) {
        this.id = id;
        this.ordem = ordem;
        this.ocupacaoCodigo = ocupacaoCodigo;
        this.rotulo = rotulo;
        this.areaM2 = areaM2;
        this.cargaInstaladaKw = cargaInstaladaKw;
        this.cargaMinimaKw = cargaMinimaKw;
        this.demandaCalculadaKva = demandaCalculadaKva;
        this.alerta = alerta;
    }

    /** Converte a entidade gravada (sem recalcular): usado na leitura do projeto. */
    public static CommercialUnitItemResponse from(CommercialUnit u) {
        return new CommercialUnitItemResponse(u.getId(), u.getOrdem(), u.getOcupacaoCodigo(), u.getAtividadeComercial(),
                u.getAreaM2(), u.getPotenciaInstaladaKw(), null, u.getDemandaCalculadaKva(), null);
    }

    public Long getId() { return id; }
    public Integer getOrdem() { return ordem; }
    public String getOcupacaoCodigo() { return ocupacaoCodigo; }
    public String getRotulo() { return rotulo; }
    public BigDecimal getAreaM2() { return areaM2; }
    public BigDecimal getCargaInstaladaKw() { return cargaInstaladaKw; }
    public BigDecimal getCargaMinimaKw() { return cargaMinimaKw; }
    public BigDecimal getDemandaCalculadaKva() { return demandaCalculadaKva; }
    public String getAlerta() { return alerta; }
}
