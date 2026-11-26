package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.MotorStarterReference;
import java.math.BigDecimal;

public class MotorStarterCatalogResponse {

    private Long id;
    private String tipoPartida;
    private BigDecimal minCv220v;
    private BigDecimal maxCv220v;
    private BigDecimal minCv380v;
    private BigDecimal maxCv380v;
    private String descricao;
    private String normaOrigem;

    public MotorStarterCatalogResponse() {
    }

    public MotorStarterCatalogResponse(Long id, String tipoPartida, BigDecimal minCv220v,
                                       BigDecimal maxCv220v, BigDecimal minCv380v,
                                       BigDecimal maxCv380v, String descricao, String normaOrigem) {
        this.id = id;
        this.tipoPartida = tipoPartida;
        this.minCv220v = minCv220v;
        this.maxCv220v = maxCv220v;
        this.minCv380v = minCv380v;
        this.maxCv380v = maxCv380v;
        this.descricao = descricao;
        this.normaOrigem = normaOrigem;
    }

    public static MotorStarterCatalogResponse from(MotorStarterReference entity) {
        if (entity == null) {
            return null;
        }
        return new MotorStarterCatalogResponse(
                entity.getId(),
                entity.getTipoPartida(),
                entity.getMinCv220v(),
                entity.getMaxCv220v(),
                entity.getMinCv380v(),
                entity.getMaxCv380v(),
                entity.getDescricao(),
                entity.getNormaOrigem()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipoPartida() {
        return tipoPartida;
    }

    public void setTipoPartida(String tipoPartida) {
        this.tipoPartida = tipoPartida;
    }

    public BigDecimal getMinCv220v() {
        return minCv220v;
    }

    public void setMinCv220v(BigDecimal minCv220v) {
        this.minCv220v = minCv220v;
    }

    public BigDecimal getMaxCv220v() {
        return maxCv220v;
    }

    public void setMaxCv220v(BigDecimal maxCv220v) {
        this.maxCv220v = maxCv220v;
    }

    public BigDecimal getMinCv380v() {
        return minCv380v;
    }

    public void setMinCv380v(BigDecimal minCv380v) {
        this.minCv380v = minCv380v;
    }

    public BigDecimal getMaxCv380v() {
        return maxCv380v;
    }

    public void setMaxCv380v(BigDecimal maxCv380v) {
        this.maxCv380v = maxCv380v;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNormaOrigem() {
        return normaOrigem;
    }

    public void setNormaOrigem(String normaOrigem) {
        this.normaOrigem = normaOrigem;
    }
}
