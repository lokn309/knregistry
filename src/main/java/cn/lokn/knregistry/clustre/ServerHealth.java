package cn.lokn.knregistry.clustre;

import cn.lokn.knregistry.http.HttpInvoker;
import cn.lokn.knregistry.model.Server;
import cn.lokn.knregistry.service.KNRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description: check health from servers.
 * @author: lokn
 * @date: 2024/04/21 21:31
 */
@Slf4j
public class ServerHealth {

    private Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    long interval = 5_000;

    public void checkServerHealth() {
        executor.scheduleAtFixedRate(() -> {
            try {
                updateServers();    // 1、更新服务器状态
                doElect();          // 2、选举leader
                syncSnapshotFromLeader();   // 3、同步快照
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void syncSnapshotFromLeader() {
        final Server self = cluster.self();
        final Server leader = cluster.leader();
        if (!self.isLeader() && self.getVersion() < leader.getVersion()) {
            log.debug(" ===>>> leader version: {}, My version: {}", leader.getVersion(), self.getVersion());
            log.debug(" ===>>> sync snapShot from leader: {}", leader);
            final SnapShot snapShot = HttpInvoker.httpGet("http://" + leader.getUrl() + "/snapShot", SnapShot.class);
            log.debug(" ===>>> sync and restore snapshot: {}", snapShot);
            KNRegistryService.restore(snapShot);
        }
    }

    private void doElect() {
        new Election().electLeader(cluster.getServers());
    }

    private void updateServers() {
        // 探活
        // 优化技巧，采用并发的方式实现
        cluster.getServers().stream().parallel().forEach(server -> {
            try {
                if (server.equals(cluster.self())) return;
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

}
