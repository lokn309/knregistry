package cn.lokn.knregistry;

import cn.lokn.knregistry.config.KNRegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KNRegistryConfigProperties.class)
public class KnregistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnregistryApplication.class, args);
    }

}
