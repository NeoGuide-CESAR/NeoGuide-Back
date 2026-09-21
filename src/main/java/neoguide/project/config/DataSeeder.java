package neoguide.project.config;

import neoguide.project.model.Project;
import neoguide.project.model.ProjectStatus;
import neoguide.project.model.ProjectType;
import neoguide.project.model.User;
import neoguide.project.repository.ProjectRepository;
import neoguide.project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Popula o banco H2 com dados de teste ao subir a aplicação.
 *
 * As senhas são codificadas com o mesmo {@link PasswordEncoder} usado no cadastro,
 * então é possível logar direto com elas.
 *
 * Usuários criados (e-mail / senha):
 *   admin@neoguide.com   / admin123
 *   maria@neoguide.com   / maria123
 *   joao@neoguide.com    / joao1234
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ProjectRepository projectRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedProjects();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        List<User> users = List.of(
                new User("Administrador", "admin@neoguide.com", passwordEncoder.encode("admin123")),
                new User("Maria Silva", "maria@neoguide.com", passwordEncoder.encode("maria123")),
                new User("João Souza", "joao@neoguide.com", passwordEncoder.encode("joao1234"))
        );

        userRepository.saveAll(users);
    }

    private void seedProjects() {
        if (projectRepository.count() > 0) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();

        List<Project> projects = List.of(
                new Project(
                        "NEO-2026-000101",
                        "Ligação nova - Residencial Jardim das Palmeiras",
                        "Projeto de ligação nova em baixa tensão para condomínio residencial com 48 unidades, "
                                + "padrão de entrada trifásico e medição agrupada.",
                        ProjectType.LIGACAO_NOVA,
                        ProjectStatus.EM_ANALISE,
                        "NBR 5410 - Instalações elétricas de baixa tensão",
                        "Eng. Maria Silva - CREA 1234567890",
                        "Salvador - BA",
                        agora.minusDays(2)
                ),
                new Project(
                        "NEO-2026-000102",
                        "Aumento de carga - Indústria Metalúrgica Andrade",
                        "Solicitação de aumento de demanda contratada de 150 kW para 400 kW, com substituição "
                                + "do transformador da subestação abrigada.",
                        ProjectType.AUMENTO_CARGA,
                        ProjectStatus.AGUARDANDO_CORRECAO,
                        "NBR 14039 - Instalações elétricas de média tensão",
                        "Eng. João Souza - CREA 9876543210",
                        "Camaçari - BA",
                        agora.minusDays(9)
                ),
                new Project(
                        "NEO-2026-000103",
                        "Geração distribuída - Usina solar fotovoltaica Recife Norte",
                        "Projeto de microgeração distribuída com 92 kWp em telhado comercial, conexão em "
                                + "baixa tensão no sistema de compensação de energia.",
                        ProjectType.GERACAO_DISTRIBUIDA,
                        ProjectStatus.APROVADO,
                        "REN ANEEL 1.059/2023 - Geração distribuída",
                        "Eng. Carla Mendes - CREA 4455667788",
                        "Recife - PE",
                        agora.minusDays(21)
                ),
                new Project(
                        "NEO-2026-000104",
                        "Extensão de rede - Loteamento Serra Verde",
                        "Extensão de rede de distribuição urbana com 1,2 km em poste de concreto e instalação "
                                + "de 3 transformadores de 75 kVA.",
                        ProjectType.EXTENSAO_REDE,
                        ProjectStatus.EM_ANALISE,
                        "NT.002 - Fornecimento em tensão primária de distribuição",
                        "Eng. Rafael Torres - CREA 2233445566",
                        "Natal - RN",
                        agora.minusDays(5)
                ),
                new Project(
                        "NEO-2026-000105",
                        "Instalação elétrica - Centro Logístico BR-101",
                        "Projeto de instalação elétrica de galpão logístico com 12.000 m², incluindo SPDA e "
                                + "quadros de distribuição secundários.",
                        ProjectType.INSTALACAO_ELETRICA,
                        ProjectStatus.REPROVADO,
                        "NBR 5419 - Proteção contra descargas atmosféricas",
                        "Eng. Patrícia Lima - CREA 3344556677",
                        "Feira de Santana - BA",
                        agora.minusDays(34)
                ),
                new Project(
                        "NEO-2026-000106",
                        "Ligação nova - Posto de recarga de veículos elétricos",
                        "Ligação nova trifásica para 6 carregadores rápidos de 60 kW, com demanda contratada "
                                + "de 380 kW e medição em média tensão.",
                        ProjectType.LIGACAO_NOVA,
                        ProjectStatus.EM_ANALISE,
                        "NBR 17019 - Instalações elétricas para recarga de veículos elétricos",
                        "Eng. Bruno Carvalho - CREA 5566778899",
                        "Campinas - SP",
                        agora.minusDays(1)
                )
        );

        projectRepository.saveAll(projects);
    }
}