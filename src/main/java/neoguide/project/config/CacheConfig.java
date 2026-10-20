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
                "catalogoFatoresAquecedoresPassagem"
        );
    }
}
