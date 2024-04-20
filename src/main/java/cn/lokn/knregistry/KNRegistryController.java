package cn.lokn.knregistry;

import cn.lokn.knregistry.model.InstanceMeta;
import cn.lokn.knregistry.service.RegistryService;
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

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> register {} @ {} ", service, instance);
        return registryService.register(service, instance);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> unregister {} @ {} ", service, instance);
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
        return registryService.renew(instance, service);
    }

    @RequestMapping("/renews")
    public Long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {} ", services, instance);
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        return registryService.version(service);
    }

}
