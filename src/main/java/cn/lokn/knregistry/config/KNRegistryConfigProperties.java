package cn.lokn.knregistry.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @description: registry config properties
 * @author: lokn
 * @date: 2024/04/20 23:44
 */
@Data
@ConfigurationProperties(prefix = "knregistry")
public class KNRegistryConfigProperties {

    private List<String> serverList;

}
