package neoguide.project.service;

import neoguide.project.dto.ProjectResponse;
import neoguide.project.model.Project;
import neoguide.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listAll() {
        List<Project> projects = projectRepository.findAllByOrderByDataSubmissaoDesc();

        List<ProjectResponse> response = new ArrayList<>();
        for (Project project : projects) {
            response.add(ProjectResponse.from(project));
        }
        return response;
    }
}