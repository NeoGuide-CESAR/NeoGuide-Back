package neoguide.project.service;

import neoguide.project.dto.EtapaEstadoResponse;
import neoguide.project.model.Project;
import neoguide.project.model.SpecialLoads;
import neoguide.project.model.TipoEdificacao;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Estado de cada etapa (1 a 5) do wizard. O servidor é quem decide; o front só exibe.
 *
 * <p>{@code etapaAtual} do projeto é a etapa mais adiantada que o usuário alcançou (1 a 6). Uma etapa só é
 * CONCLUIDA quando o usuário já passou dela e o que ela exige está gravado. As etapas são sequenciais: a etapa N
 * só fica disponível quando 1 a N-1 estão concluídas ou não se aplicam. A etapa 4 (Comercial) é sempre opcional e
 * nunca depende do tipo de edificação.
 */
@Service
public class EstadoEtapasService {

    static final String[] NOMES = {"Projeto", "Residencial", "Área comum", "Comercial", "VE e bomba de incêndio"};

    private final ResidentialTypologyRepository residentialTypologyRepository;
    private final CommercialUnitRepository commercialUnitRepository;
    private final SpecialLoadsRepository specialLoadsRepository;

    public EstadoEtapasService(ResidentialTypologyRepository residentialTypologyRepository,
                               CommercialUnitRepository commercialUnitRepository,
                               SpecialLoadsRepository specialLoadsRepository) {
        this.residentialTypologyRepository = residentialTypologyRepository;
        this.commercialUnitRepository = commercialUnitRepository;
        this.specialLoadsRepository = specialLoadsRepository;
    }

    public List<EtapaEstadoResponse> etapas(Project project) {
        int alcancada = project.getEtapaAtual() == null ? 1 : project.getEtapaAtual();
        boolean[] naoSeAplica = new boolean[6];
        boolean[] dadosOk = new boolean[6];

        naoSeAplica[2] = project.getTipoEdificacao() == TipoEdificacao.COMERCIAL;
        naoSeAplica[5] = !project.isPossuiVe() && !project.isPossuiIncendio();

        dadosOk[1] = naoVazio(project.getTitulo()) && project.getTipoEdificacao() != null
                && naoVazio(project.getCep()) && naoVazio(project.getEndereco());
        dadosOk[2] = !residentialTypologyRepository.findByProjectIdOrderByIdAsc(project.getId()).isEmpty();
        dadosOk[3] = true; // área comum é opcional
        dadosOk[4] = true; // comercial é opcional, para qualquer tipo de edificação
        dadosOk[5] = especiaisOk(project);

        List<EtapaEstadoResponse> estados = new ArrayList<>();
        boolean anterioresConcluidas = true;
        for (int n = 1; n <= 5; n++) {
            String estado;
            boolean concluida;
            if (naoSeAplica[n]) {
                estado = "NAO_SE_APLICA";
                concluida = true;
            } else if (!anterioresConcluidas) {
                // A sequência vale primeiro: nada se conclui nem abre com uma etapa anterior pendente.
                estado = "BLOQUEADA";
                concluida = false;
            } else if (n < alcancada && dadosOk[n]) {
                estado = "CONCLUIDA";
                concluida = true;
            } else {
                estado = n == Math.min(alcancada, 5) ? "ATUAL" : "DISPONIVEL";
                concluida = false;
            }
            estados.add(new EtapaEstadoResponse(n, NOMES[n - 1], estado));
            anterioresConcluidas = anterioresConcluidas && concluida;
        }
        return estados;
    }

    /** Tipo usado no enquadramento BT/MT (ver {@link EnquadramentoEdificacao}). */
    public String tipoEnquadramento(Project project) {
        boolean temEspacos = !commercialUnitRepository.findByProjectIdOrderByIdAsc(project.getId()).isEmpty();
        TipoEdificacao derivado = EnquadramentoEdificacao.derivar(project.getTipoEdificacao(), temEspacos);
        return derivado == null ? null : derivado.name();
    }

    private boolean especiaisOk(Project project) {
        SpecialLoads s = specialLoadsRepository.findByProjectId(project.getId()).orElse(null);
        boolean ve = !project.isPossuiVe() || (s != null && s.getQtdPontosVe() != null && s.getQtdPontosVe() > 0
                && s.getPotenciaEstacaoVeKw() != null && s.getPotenciaEstacaoVeKw().signum() > 0);
        boolean incendio = !project.isPossuiIncendio() || (s != null && s.getPotenciaBombaIncendioCv() != null
                && s.getPotenciaBombaIncendioCv().signum() > 0);
        return ve && incendio;
    }

    private static boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }
}
