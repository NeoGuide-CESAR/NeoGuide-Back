package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_fatores_coincidencia")
public class CoincidenceFactorReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_apartamentos", nullable = false)
    private Integer totalApartamentos;

    @Column(name = "fator_coincidencia", nullable = false, precision = 6, scale = 4)
    private BigDecimal fatorCoincidencia;

    @Column(name = "fator_coincidencia_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal fatorCoincidenciaPercent;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public CoincidenceFactorReference() {
    }

    public CoincidenceFactorReference(Integer totalApartamentos, BigDecimal fatorCoincidencia,
                                      BigDecimal fatorCoincidenciaPercent, String normaOrigem) {
        this.totalApartamentos = totalApartamentos;
        this.fatorCoincidencia = fatorCoincidencia;
        this.fatorCoincidenciaPercent = fatorCoincidenciaPercent;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getFatorCoincidenciaPercent() {
        return fatorCoincidenciaPercent;
    }

    public void setFatorCoincidenciaPercent(BigDecimal fatorCoincidenciaPercent) {
        this.fatorCoincidenciaPercent = fatorCoincidenciaPercent;
    }

    public String getNormaOrigem() {
        return normaOrigem;
    }

    public void setNormaOrigem(String normaOrigem) {
        this.normaOrigem = normaOrigem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoincidenceFactorReference that)) return false;
        return Objects.equals(id, that.id) || Objects.equals(totalApartamentos, that.totalApartamentos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalApartamentos);
    }
}
