package neoguide.project.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationConfigurationPropertiesTest {

    private final YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();

    @Test
    @DisplayName("Garante que o arquivo application.yaml principal existe e define as propriedades do PostgreSQL e Hibernate")
    void testApplicationYamlLoadsPostgreSQLProperties() throws IOException {
        ClassPathResource resource = new ClassPathResource("application.yaml");
        List<PropertySource<?>> propertySources = yamlLoader.load("applicationConfig", resource);

        assertFalse(propertySources.isEmpty(), "application.yaml deve conter property sources válidas");
        PropertySource<?> source = propertySources.get(0);

        assertEquals("${SPRING_PROFILES_ACTIVE:dev}", source.getProperty("spring.profiles.active"),
                "Sem SPRING_PROFILES_ACTIVE, o perfil padrão deve ser dev");
        assertEquals("org.hibernate.dialect.PostgreSQLDialect",
                source.getProperty("spring.jpa.properties.hibernate.dialect"));

        source = yamlLoader.load("dev", new ClassPathResource("application-dev.yaml")).get(0);
        assertEquals("${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/neoguide_db}",
                source.getProperty("spring.datasource.url"));
        assertEquals("${SPRING_DATASOURCE_USERNAME:neoguide_user}",
                source.getProperty("spring.datasource.username"));
        assertEquals("${SPRING_DATASOURCE_PASSWORD:neoguide_password}",
                source.getProperty("spring.datasource.password"));
        assertEquals("org.postgresql.Driver",
                source.getProperty("spring.datasource.driver-class-name"));
        assertEquals("${SPRING_JPA_HIBERNATE_DDL_AUTO:update}",
                source.getProperty("spring.jpa.hibernate.ddl-auto"));
    }

    @Test
    @DisplayName("Garante que as propriedades padrão são resolvidas com os valores esperados na ausência de variáveis de ambiente")
    void testDefaultResolvedPropertyValues() throws IOException {
        MockEnvironment environment = new MockEnvironment();
        carregar(environment, "application-dev.yaml");
        carregar(environment, "application.yaml");

        assertEquals("jdbc:postgresql://localhost:5432/neoguide_db",
                environment.getProperty("spring.datasource.url"));
        assertEquals("neoguide_user",
                environment.getProperty("spring.datasource.username"));
        assertEquals("neoguide_password",
                environment.getProperty("spring.datasource.password"));
        assertEquals("org.postgresql.Driver",
                environment.getProperty("spring.datasource.driver-class-name"));
        assertEquals("update",
                environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("org.hibernate.dialect.PostgreSQLDialect",
                environment.getProperty("spring.jpa.properties.hibernate.dialect"));
        assertEquals("http://localhost:4200",
                environment.getProperty("spring.configuration.host-front-end"));
    }

    @Test
    @DisplayName("Garante que variáveis de ambiente sobrescrevem as propriedades de conexão com o banco")
    void testEnvironmentVariablesOverrideDatasourceProperties() throws IOException {
        MockEnvironment environment = new MockEnvironment();
        MockPropertySource envVariables = new MockPropertySource();
        envVariables.setProperty("SPRING_DATASOURCE_URL", "jdbc:postgresql://neoguide-db:5432/neoguide_prod");
        envVariables.setProperty("SPRING_DATASOURCE_USERNAME", "prod_user");
        envVariables.setProperty("SPRING_DATASOURCE_PASSWORD", "prod_secret");
        envVariables.setProperty("SPRING_JPA_HIBERNATE_DDL_AUTO", "validate");
        environment.getPropertySources().addFirst(envVariables);

        carregar(environment, "application-dev.yaml");
        carregar(environment, "application.yaml");

        assertEquals("jdbc:postgresql://neoguide-db:5432/neoguide_prod",
                environment.getProperty("spring.datasource.url"));
        assertEquals("prod_user",
                environment.getProperty("spring.datasource.username"));
        assertEquals("prod_secret",
                environment.getProperty("spring.datasource.password"));
        assertEquals("validate",
                environment.getProperty("spring.jpa.hibernate.ddl-auto"));
    }

    @Test
    @DisplayName("Perfil prod não tem valores padrão para credenciais, chave JWT e origem do front, e não cria dados de demonstração")
    void testProdProfileRequiresEnvironmentVariables() throws IOException {
        MockEnvironment environment = new MockEnvironment();
        carregar(environment, "application-prod.yaml");
        for (String chave : List.of("spring.datasource.url", "spring.datasource.username", "spring.datasource.password",
                "security.jwt.secret", "spring.configuration.host-front-end")) {
            assertThrows(IllegalArgumentException.class, () -> environment.getProperty(chave),
                    chave + " deve ser obrigatória em prod (sem padrão)");
        }
        assertEquals("false", environment.getProperty("springdoc.swagger-ui.enabled"));
        assertEquals("never", environment.getProperty("server.error.include-message"));
        assertEquals("false", environment.getProperty("neoguide.seed.demo-data"));
    }

    @Test
    @DisplayName("Perfil prod lê banco, JWT e CORS das variáveis de ambiente")
    void testProdProfileReadsEnvironmentVariables() throws IOException {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:5432/neoguide")
                .withProperty("SPRING_DATASOURCE_USERNAME", "u")
                .withProperty("SPRING_DATASOURCE_PASSWORD", "p")
                .withProperty("SECURITY_JWT_SECRET", "segredo-de-producao-com-mais-de-32-bytes-ok")
                .withProperty("HOST_FRONT_END", "https://neoguide.exemplo.com");
        carregar(environment, "application-prod.yaml");

        assertEquals("jdbc:postgresql://db:5432/neoguide", environment.getProperty("spring.datasource.url"));
        assertEquals("segredo-de-producao-com-mais-de-32-bytes-ok", environment.getProperty("security.jwt.secret"));
        assertEquals("https://neoguide.exemplo.com", environment.getProperty("spring.configuration.host-front-end"));
    }

    private void carregar(MockEnvironment environment, String arquivo) throws IOException {
        for (PropertySource<?> ps : yamlLoader.load(arquivo, new ClassPathResource(arquivo))) {
            environment.getPropertySources().addLast(ps);
        }
    }

    @Test
    @DisplayName("Garante que Lombok não está presente no classpath em conformidade com as diretrizes do projeto")
    void testLombokIsNotPresentOnClasspath() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("lombok.Getter"),
                "Lombok não deve estar presente no classpath");
    }
}
