package cn.lokn.knregistry;

import cn.lokn.knregistry.clustre.Cluster;
import cn.lokn.knregistry.clustre.SnapShot;
import cn.lokn.knregistry.http.HttpInvoker;
import cn.lokn.knregistry.model.InstanceMeta;
import cn.lokn.knregistry.model.Server;
import cn.lokn.knregistry.service.KNRegistryService;
import cn.lokn.knregistry.service.RegistryService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

/**
 * @description: Rest controller for registry service
 * @author: lokn
 * @date: 2024/04/13 19:49
 */
@Slf4j
@RestController
public class KNRegistryController {

    @Autowired
    private RegistryService registryService;

    @Autowired
    private Cluster cluster;

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> register {} @ {} ", service, instance);
        final Server self = cluster.self();
        if (!self.isLeader()) {
            String suffixPath = "/reg?service=" + service;
            return this.callLeader(suffixPath, instance);
        }
        return registryService.register(service, instance);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> unregister {} @ {} ", service, instance);
        final Server self = cluster.self();
        if (!self.isLeader()) {
            String suffixPath = "/unreg?service=" + service;
            return this.callLeader(suffixPath, instance);
        }
        return registryService.unregister(service, instance);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam String service) {
        log.info(" ===> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renew")
    public Long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {} ", service, instance);
        checkLeader();
        return registryService.renew(instance, service);
    }

    @RequestMapping("/renews")
    public Long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {} ", services, instance);
        checkLeader();
        return registryService.renew(instance, services.split(","));
    }

    private void checkLeader() {
        if (!cluster.self().isLeader()) {
            throw new RuntimeException("current server is not a leader, the leader is " + cluster.leader().getVersion());
        }
    }

    private InstanceMeta callLeader(String suffixPath, InstanceMeta instance) {
        Server leader = cluster.leader();
        return HttpInvoker.httpPost(leader.getUrl() + suffixPath, JSON.toJSONString(instance), InstanceMeta.class);
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        return registryService.version(service);
    }

    @RequestMapping("/info")
    public Server info() {
        log.info(" ===> info: {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> cluster: {}", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader() {
        log.info(" ===> cluster: {}", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/snapShot")
    public SnapShot snapShot() {
        return KNRegistryService.snapShot();
    }

}
