package cn.lokn.knregistry.clustre;

import cn.lokn.knregistry.config.KNRegistryConfigProperties;
import cn.lokn.knregistry.http.HttpInvoker;
import cn.lokn.knregistry.model.Server;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    long timeout = 5_000;

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

        executor.scheduleAtFixedRate(() -> {
            updateServers();
            electLeader();
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    private void electLeader() {
        final List<Server> masters = servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).collect(Collectors.toList());
        if (masters.isEmpty()) {
            log.info(" ===>>> elect for no leader: {}", servers);
            elect();
        } else if (masters.size() > 1) {
            log.info(" ===>>> elect for more than one leader: {}", servers);
            elect();
        } else {
            log.info(" ===>>> no need election for leader: {}", masters.get(0));
        }
    }

    private void elect() {
        // 1、各种节点自己选，算法保证大家选的是同一个
        // 2、外部有一个分布式锁，谁拿到锁，谁是主
        // 3、分布式一致性算法，比如paxos、raft，，很复杂

        Server condition = null;
        for (Server server : servers) {
            if (!server.isStatus()) continue;
            server.setLeader(false);
            if (condition == null) {
                condition = server;
            } else {
                // 这里取hash值小的为leader
                if (condition.hashCode() > server.hashCode()) {
                    condition = server;
                }
            }
        }
        if (condition != null) {
            condition.setLeader(true);
            log.info(" ===>>> elect for leader: {}", condition);
        }
    }

    private void updateServers() {
        // 探活
        servers.forEach(server -> {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.debug(" ===>>> health check success for {}", serverInfo);
                if (serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.error(" ===>>> health check failed for {},", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self() {
        return MYSELF;
    }

    public Server leader() {
        return servers.stream().filter(Server::isLeader).findFirst().orElse(null);
    }

}
