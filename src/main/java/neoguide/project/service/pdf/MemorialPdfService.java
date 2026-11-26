package neoguide.project.service.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

import neoguide.project.exception.BadRequestException;
import neoguide.project.exception.ForbiddenException;
import neoguide.project.exception.ResourceNotFoundException;
import neoguide.project.model.CommercialUnit;
import neoguide.project.model.CommonAreaLoad;
import neoguide.project.model.DemandResults;
import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ResidentialTypology;
import neoguide.project.model.SpecialLoads;
import neoguide.project.repository.CommercialUnitRepository;
import neoguide.project.repository.CommonAreaLoadRepository;
import neoguide.project.repository.DemandResultsRepository;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.ResidentialTypologyRepository;
import neoguide.project.repository.SpecialLoadsRepository;

@Service
public class MemorialPdfService {

    private final ProjectRepository projectRepository;
    private final DemandResultsRepository demandResultsRepository;
    private final ResidentialTypologyRepository residentialTypologyRepository;
    private final CommonAreaLoadRepository commonAreaLoadRepository;
    private final CommercialUnitRepository commercialUnitRepository;
    private final SpecialLoadsRepository specialLoadsRepository;
    private final MemorialSectionRenderer sectionRenderer;

    public MemorialPdfService(ProjectRepository projectRepository,
                              DemandResultsRepository demandResultsRepository,
                              ResidentialTypologyRepository residentialTypologyRepository,
                              CommonAreaLoadRepository commonAreaLoadRepository,
                              CommercialUnitRepository commercialUnitRepository,
                              SpecialLoadsRepository specialLoadsRepository,
                              MemorialSectionRenderer sectionRenderer) {
        this.projectRepository = projectRepository;
        this.demandResultsRepository = demandResultsRepository;
        this.residentialTypologyRepository = residentialTypologyRepository;
        this.commonAreaLoadRepository = commonAreaLoadRepository;
        this.commercialUnitRepository = commercialUnitRepository;
        this.specialLoadsRepository = specialLoadsRepository;
        this.sectionRenderer = sectionRenderer;
    }

    @Transactional(readOnly = true)
    public byte[] generateMemorialPdf(Long projectId, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .filter(Project::isAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id: " + projectId));

        if (project.getUser() == null || !project.getUser().getEmail().equals(userEmail)) {
            throw new ForbiddenException("Acesso não autorizado aos dados do projeto.");
        }

        // Validação obrigatória da condição de cálculo oficial consolidado
        if (project.getStatus() == ProjectStatus.RASCUNHO) {
            throw new BadRequestException("Projeto não consolidado. Efetue o cálculo oficial antes de emitir o memorial.");
        }

        DemandResults demandResults = demandResultsRepository.findByProjectId(projectId)
                .orElseThrow(() -> new BadRequestException("Projeto não consolidado. Efetue o cálculo oficial antes de emitir o memorial."));

        List<ResidentialTypology> typologies = residentialTypologyRepository.findByProjectIdOrderByIdAsc(projectId);
        List<CommonAreaLoad> commonLoads = commonAreaLoadRepository.findByProjectId(projectId);
        List<CommercialUnit> commercialUnits = commercialUnitRepository.findByProjectId(projectId);
        SpecialLoads specialLoads = specialLoadsRepository.findByProjectId(projectId).orElse(null);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Registro do evento de paginação ("Página X de Y")
            PdfPageEventsHelper eventHelper = new PdfPageEventsHelper(project.getProtocolo());
            writer.setPageEvent(eventHelper);

            document.open();

            // Renderização sequencial das 7 Seções Normativas
            sectionRenderer.renderSection1Header(document, project);
            sectionRenderer.renderSection2Supply(document, project, demandResults);
            sectionRenderer.renderSection3ResidentialQ1(document, project, demandResults, typologies);
            sectionRenderer.renderSection4CommonAreasQ2(document, project, demandResults, commonLoads);
            sectionRenderer.renderSection5SpecialCommercialQ3(document, project, demandResults, commercialUnits, specialLoads);
            sectionRenderer.renderSection6SynthesisEntranceQ4(document, project, demandResults);
            sectionRenderer.renderSection7ComplianceSignature(document, project, demandResults);

            document.close();
            return baos.toByteArray();
        } catch (BadRequestException | ForbiddenException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar documento PDF do Memorial Descritivo: " + e.getMessage(), e);
        }
    }
}
