package cn.lokn.knregistry.clustre;

import cn.lokn.knregistry.config.KNRegistryConfigProperties;
import cn.lokn.knregistry.model.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description: Registry cluster
 * @author: lokn
 * @date: 2024/04/16 20:28
 */
@Slf4j
public class Cluster {

    KNRegistryConfigProperties registryConfigProperties;

    public Cluster(KNRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    private List<Server> servers;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    long timeout = 5_000;

    public void init() {
        servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            Server server = new Server();
            server.setUrl(url);
            server.setStatus(false);
            server.setLeader(false);
            server.setVersion(-1);
            servers.add(server);
        }

        executor.scheduleAtFixedRate(() -> {
            updateServers();
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    private void updateServers() {

    }

}
