package neoguide.project.dto.catalogo;

import com.fasterxml.jackson.annotation.JsonProperty;
import neoguide.project.model.normativo.AirConditionerCatalog;

import java.math.BigDecimal;

public class AirConditionerCatalogResponse {

    private Long id;
    private Integer capacidadeBtu;
    private BigDecimal potenciaAtivaW;
    private BigDecimal potenciaAparenteVa;
    private Integer tensaoV;

    @JsonProperty("corrente220vA")
    private BigDecimal corrente220vA;

    private BigDecimal fatorPotencia;
    private String normaOrigem;

    public AirConditionerCatalogResponse() {
    }

    public AirConditionerCatalogResponse(Long id, Integer capacidadeBtu, BigDecimal potenciaAtivaW,
                                         BigDecimal potenciaAparenteVa, Integer tensaoV,
                                         BigDecimal corrente220vA, BigDecimal fatorPotencia,
                                         String normaOrigem) {
        this.id = id;
        this.capacidadeBtu = capacidadeBtu;
        this.potenciaAtivaW = potenciaAtivaW;
        this.potenciaAparenteVa = potenciaAparenteVa;
        this.tensaoV = tensaoV;
        this.corrente220vA = corrente220vA;
        this.fatorPotencia = fatorPotencia;
        this.normaOrigem = normaOrigem;
    }

    public static AirConditionerCatalogResponse from(AirConditionerCatalog entity) {
        return new AirConditionerCatalogResponse(
                entity.getId(),
                entity.getCapacidadeBtu(),
                entity.getPotenciaAtivaW(),
                entity.getPotenciaAparenteVa(),
                entity.getTensaoV(),
                entity.getCorrenteNominalA(),
                entity.getFatorPotencia(),
                entity.getNormaOrigem()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCapacidadeBtu() {
        return capacidadeBtu;
    }

    public void setCapacidadeBtu(Integer capacidadeBtu) {
        this.capacidadeBtu = capacidadeBtu;
    }

    public BigDecimal getPotenciaAtivaW() {
        return potenciaAtivaW;
    }

    public void setPotenciaAtivaW(BigDecimal potenciaAtivaW) {
        this.potenciaAtivaW = potenciaAtivaW;
    }

    public BigDecimal getPotenciaAparenteVa() {
        return potenciaAparenteVa;
    }

    public void setPotenciaAparenteVa(BigDecimal potenciaAparenteVa) {
        this.potenciaAparenteVa = potenciaAparenteVa;
    }

    public Integer getTensaoV() {
        return tensaoV;
    }

    public void setTensaoV(Integer tensaoV) {
        this.tensaoV = tensaoV;
    }

    @JsonProperty("corrente220vA")
    public BigDecimal getCorrente220vA() {
        return corrente220vA;
    }

    @JsonProperty("corrente220vA")
    public void setCorrente220vA(BigDecimal corrente220vA) {
        this.corrente220vA = corrente220vA;
    }

    public BigDecimal getFatorPotencia() {
        return fatorPotencia;
    }

    public void setFatorPotencia(BigDecimal fatorPotencia) {
        this.fatorPotencia = fatorPotencia;
    }

    public String getNormaOrigem() {
        return normaOrigem;
    }

    public void setNormaOrigem(String normaOrigem) {
        this.normaOrigem = normaOrigem;
    }
}
