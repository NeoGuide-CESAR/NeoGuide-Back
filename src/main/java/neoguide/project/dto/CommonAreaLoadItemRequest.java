package neoguide.project.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CommonAreaLoadItemRequest {

    private String origem; // "CATALOGO" ou "PERSONALIZADO"
    private String submodulo; // "ILUMINACAO_TUG", "CLIMATIZACAO", "MOTORES", "COCCAO", "AQUECIMENTO"
    private Long catalogoMotorId;
    private Long catalogoClimaId;
    private Long catalogoEquipamentoId;
    private String tipoEquipamento;
    private String descricao;
    private Integer quantidade;
    private BigDecimal potenciaNominalKw;
    private BigDecimal fatorPotencia;
    private BigDecimal fatorDemanda;

    @JsonProperty("isReserva")
    @JsonAlias({"reserva", "isReserva"})
    private Boolean isReserva = Boolean.FALSE;

    public CommonAreaLoadItemRequest() {
    }

    public CommonAreaLoadItemRequest(String origem, String submodulo, Long catalogoMotorId,
                                     Long catalogoClimaId, Long catalogoEquipamentoId,
                                     String tipoEquipamento, String descricao, Integer quantidade,
                                     BigDecimal potenciaNominalKw, BigDecimal fatorPotencia,
                                     BigDecimal fatorDemanda, Boolean isReserva) {
        this.origem = origem;
        this.submodulo = submodulo;
        this.catalogoMotorId = catalogoMotorId;
        this.catalogoClimaId = catalogoClimaId;
        this.catalogoEquipamentoId = catalogoEquipamentoId;
        this.tipoEquipamento = tipoEquipamento;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.potenciaNominalKw = potenciaNominalKw;
        this.fatorPotencia = fatorPotencia;
        this.fatorDemanda = fatorDemanda;
        this.isReserva = isReserva != null ? isReserva : Boolean.FALSE;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getSubmodulo() {
        return submodulo;
    }

    public void setSubmodulo(String submodulo) {
        this.submodulo = submodulo;
    }

    public Long getCatalogoMotorId() {
        return catalogoMotorId;
    }

    public void setCatalogoMotorId(Long catalogoMotorId) {
        this.catalogoMotorId = catalogoMotorId;
    }

    public Long getCatalogoClimaId() {
        return catalogoClimaId;
    }

    public void setCatalogoClimaId(Long catalogoClimaId) {
        this.catalogoClimaId = catalogoClimaId;
    }

    public Long getCatalogoEquipamentoId() {
        return catalogoEquipamentoId;
    }

    public void setCatalogoEquipamentoId(Long catalogoEquipamentoId) {
        this.catalogoEquipamentoId = catalogoEquipamentoId;
    }

    public String getTipoEquipamento() {
        return tipoEquipamento;
    }

    public void setTipoEquipamento(String tipoEquipamento) {
        this.tipoEquipamento = tipoEquipamento;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPotenciaNominalKw() {
        return potenciaNominalKw;
    }

    public void setPotenciaNominalKw(BigDecimal potenciaNominalKw) {
        this.potenciaNominalKw = potenciaNominalKw;
    }

    public BigDecimal getFatorPotencia() {
        return fatorPotencia;
    }

    public void setFatorPotencia(BigDecimal fatorPotencia) {
        this.fatorPotencia = fatorPotencia;
    }

    public BigDecimal getFatorDemanda() {
        return fatorDemanda;
    }

    public void setFatorDemanda(BigDecimal fatorDemanda) {
        this.fatorDemanda = fatorDemanda;
    }

    public boolean isReserva() {
        return Boolean.TRUE.equals(isReserva);
    }

    public void setReserva(Boolean reserva) {
        this.isReserva = reserva != null ? reserva : Boolean.FALSE;
    }

    public void setIsReserva(Boolean isReserva) {
        this.isReserva = isReserva != null ? isReserva : Boolean.FALSE;
    }

    @JsonProperty("isReserva")
    public Boolean getIsReserva() {
        return Boolean.TRUE.equals(isReserva);
    }
}
