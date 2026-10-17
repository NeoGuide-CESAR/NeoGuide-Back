package neoguide.project.model.normativo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "catalogo_dimensionamento_entrada")
public class ServiceEntranceReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tensao_fornecimento", nullable = false, length = 20)
    private String tensaoFornecimento; // "380_220V" ou "220_127V"

    @Column(name = "max_demanda_kva", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxDemandaKva;

    @Column(name = "quantidade_circuitos", nullable = false)
    private Integer quantidadeCircuitos;

    @Column(name = "secao_condutor_mm2", nullable = false, length = 30)
    private String secaoCondutorMm2;

    @Column(name = "duto_pead_mm", length = 30)
    private String dutoPeadMm;

    @Column(name = "duto_aco_mm", length = 30)
    private String dutoAcoMm;

    @Column(name = "corrente_total_a", nullable = false)
    private Integer correnteTotalA;

    @Column(name = "disjuntor_carcaca", length = 30)
    private String disjuntorCarcaca;

    @Column(name = "disjuntor_corrente_a", nullable = false)
    private Integer disjuntorCorrenteA;

    @Column(name = "norma_origem", nullable = false, length = 60)
    private String normaOrigem;

    public ServiceEntranceReference() {
    }

    public ServiceEntranceReference(String tensaoFornecimento, BigDecimal maxDemandaKva, Integer quantidadeCircuitos,
                                    String secaoCondutorMm2, String dutoPeadMm, String dutoAcoMm,
                                    Integer correnteTotalA, String disjuntorCarcaca, Integer disjuntorCorrenteA,
                                    String normaOrigem) {
        this.tensaoFornecimento = tensaoFornecimento;
        this.maxDemandaKva = maxDemandaKva;
        this.quantidadeCircuitos = quantidadeCircuitos;
        this.secaoCondutorMm2 = secaoCondutorMm2;
        this.dutoPeadMm = dutoPeadMm;
        this.dutoAcoMm = dutoAcoMm;
        this.correnteTotalA = correnteTotalA;
        this.disjuntorCarcaca = disjuntorCarcaca;
        this.disjuntorCorrenteA = disjuntorCorrenteA;
        this.normaOrigem = normaOrigem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTensaoFornecimento() {
        return tensaoFornecimento;
    }

    public void setTensaoFornecimento(String tensaoFornecimento) {
        this.tensaoFornecimento = tensaoFornecimento;
    }

    public BigDecimal getMaxDemandaKva() {
        return maxDemandaKva;
    }

    public void setMaxDemandaKva(BigDecimal maxDemandaKva) {
        this.maxDemandaKva = maxDemandaKva;
    }

    public Integer getQuantidadeCircuitos() {
        return quantidadeCircuitos;
    }

    public void setQuantidadeCircuitos(Integer quantidadeCircuitos) {
        this.quantidadeCircuitos = quantidadeCircuitos;
    }

    public String getSecaoCondutorMm2() {
        return secaoCondutorMm2;
    }

    public void setSecaoCondutorMm2(String secaoCondutorMm2) {
        this.secaoCondutorMm2 = secaoCondutorMm2;
    }

    public String getDutoPeadMm() {
        return dutoPeadMm;
    }

    public void setDutoPeadMm(String dutoPeadMm) {
        this.dutoPeadMm = dutoPeadMm;
    }

    public String getDutoAcoMm() {
        return dutoAcoMm;
    }

    public void setDutoAcoMm(String dutoAcoMm) {
        this.dutoAcoMm = dutoAcoMm;
    }

    public Integer getCorrenteTotalA() {
        return correnteTotalA;
    }

    public void setCorrenteTotalA(Integer correnteTotalA) {
        this.correnteTotalA = correnteTotalA;
    }

    public String getDisjuntorCarcaca() {
        return disjuntorCarcaca;
    }

    public void setDisjuntorCarcaca(String disjuntorCarcaca) {
        this.disjuntorCarcaca = disjuntorCarcaca;
    }

    public Integer getDisjuntorCorrenteA() {
        return disjuntorCorrenteA;
    }

    public void setDisjuntorCorrenteA(Integer disjuntorCorrenteA) {
        this.disjuntorCorrenteA = disjuntorCorrenteA;
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
        if (!(o instanceof ServiceEntranceReference that)) return false;
        return Objects.equals(id, that.id) || (Objects.equals(tensaoFornecimento, that.tensaoFornecimento)
                && Objects.equals(maxDemandaKva, that.maxDemandaKva)
                && Objects.equals(disjuntorCorrenteA, that.disjuntorCorrenteA));
    }

    @Override
    public int hashCode() {
        return Objects.hash(tensaoFornecimento, maxDemandaKva, disjuntorCorrenteA);
    }
}
