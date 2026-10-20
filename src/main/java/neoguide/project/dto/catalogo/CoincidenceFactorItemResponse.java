package neoguide.project.dto.catalogo;

import neoguide.project.model.normativo.CoincidenceFactorReference;

import java.math.BigDecimal;

public class CoincidenceFactorItemResponse {

    private Integer totalApartamentos;
    private BigDecimal fatorCoincidencia;

    public CoincidenceFactorItemResponse() {
    }

    public CoincidenceFactorItemResponse(Integer totalApartamentos, BigDecimal fatorCoincidencia) {
        this.totalApartamentos = totalApartamentos;
        this.fatorCoincidencia = fatorCoincidencia;
    }

    public static CoincidenceFactorItemResponse from(CoincidenceFactorReference ref) {
        if (ref == null) return null;
        return new CoincidenceFactorItemResponse(
                ref.getTotalApartamentos(),
                ref.getFatorCoincidencia()
        );
    }

    public Integer getTotalApartamentos() {
        return totalApartamentos;
    }

    public void setTotalApartamentos(Integer totalApartamentos) {
        this.totalApartamentos = totalApartamentos;
    }

    public BigDecimal getFatorCoincidencia() {
        return fatorCoincidencia;
    }

    public void setFatorCoincidencia(BigDecimal fatorCoincidencia) {
        this.fatorCoincidencia = fatorCoincidencia;
    }
}
