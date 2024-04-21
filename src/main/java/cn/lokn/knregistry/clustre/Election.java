package cn.lokn.knregistry.clustre;

import cn.lokn.knregistry.model.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @description: elect for server
 * @author: lokn
 * @date: 2024/04/21 21:40
 */
@Slf4j
public class Election {

    public void electLeader(List<Server> servers) {
        final List<Server> masters = servers.stream().filter(Server::isStatus)
                .filter(Server::isLeader).toList();
        if (masters.isEmpty()) {
            log.info(" ===>>> elect for no leader: {}", servers);
            elect(servers);
        } else if (masters.size() > 1) {
            log.info(" ===>>> elect for more than one leader: {}", servers);
            elect(servers);
        } else {
            log.debug(" ===>>> no need election for leader: {}", masters.get(0));
        }
    }

    private void elect(List<Server> servers) {
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

}
