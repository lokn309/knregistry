package cn.lokn.knregistry.config;

import cn.lokn.knregistry.health.HealthChecker;
import cn.lokn.knregistry.health.KNHealthChecker;
import cn.lokn.knregistry.service.KNRegistryService;
import cn.lokn.knregistry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: configuration for all beans
 * @author: lokn
 * @date: 2024/04/13 19:50
 */
@Configuration
public class KNRegistryConfig {

    @Bean
    public RegistryService registryService() {
        return new KNRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(@Autowired RegistryService registryService) {
        return new KNHealthChecker(registryService);
    }

}
