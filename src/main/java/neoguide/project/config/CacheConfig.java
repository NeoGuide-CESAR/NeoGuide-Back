package neoguide.project.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                // Caches residenciais (US-08)
                "catalogoFaixasArea",
                "catalogoFatoresCoincidencia",
                "catalogoFatoresSeguranca",
                // Caches de áreas comuns e serviços (US-11)
                "catalogoMotoresPadrao",
                "catalogoAparelhosClimatizacao",
                "catalogoFatoresArCondicionado",
                "catalogoFatoresCooktop",
                "catalogoFatoresBoilers",
                "catalogoFatoresAquecedoresPassagem",
                // Caches de cargas especiais e ocupação (US-15)
                "catalogo_fatores_ve",
                "catalogoFatoresVe",
                "catalogo_cargas_ocupacao",
                "catalogoCargasOcupacao",
                "catalogo_fatores_piscina",
                "catalogoFatoresPiscina",
                "catalogoBombasPiscina",
                // Caches de motores monofásicos, partidas e tomadas mínimas (US-16)
                "catalogoMotoresMonofasicos",
                "catalogoDispositivosPartida",
                "catalogoTomadasMinimas"
        );
    }
}
