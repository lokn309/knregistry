package cn.lokn.knregistry.clustre;

import cn.lokn.knregistry.config.KNRegistryConfigProperties;
import cn.lokn.knregistry.model.Server;
import cn.lokn.knregistry.service.KNRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: Registry cluster
 * @author: lokn
 * @date: 2024/04/16 20:28
 */
@Slf4j
public class Cluster {

    @Value("${server.port}")
    String port;

    String host;

    Server MYSELF;

    KNRegistryConfigProperties registryConfigProperties;

    public Cluster(KNRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    @Getter
    private List<Server> servers;

    public void init() {
        try {
            // 获取非回环地址的ip信息，该方法需用到spring-cloud 4.1.1 的版本
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            log.info(" ===>> findFirstNonLoopbackHostInfo: {}", host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        MYSELF = new Server("http://" + host + ":" + port, true, false, -1);
        log.info(" ===>>> myself = {}", MYSELF);

        initServer();
        new ServerHealth(this).checkServerHealth();
    }

    private void initServer() {
        servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            if (url.equals(MYSELF.getUrl())) {
                servers.add(MYSELF);
            } else {
                Server server = new Server();
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1);
                servers.add(server);
            }
        }
    }

    public Server self() {
        MYSELF.setVersion(KNRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader() {
        return servers.stream().filter(Server::isLeader).findFirst().orElse(null);
    }

}
