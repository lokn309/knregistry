package cn.lokn.knregistry.health;

import cn.lokn.knregistry.model.InstanceMeta;
import cn.lokn.knregistry.service.KNRegistryService;
import cn.lokn.knregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: lokn
 * @date: 2024/04/13 20:42
 */
@Slf4j
public class KNHealthChecker implements HealthChecker {

    private RegistryService registryService;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    long timeout = 20_000;

    public KNHealthChecker(RegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(
            () -> {
                log.info(" ===> Health checker running...");
                long now = System.currentTimeMillis();
                KNRegistryService.TIMESTAMPS.keySet().stream().forEach(serviceAndInstance -> {
                    final Long time = KNRegistryService.TIMESTAMPS.get(serviceAndInstance);
                    if (now - time > timeout) {
                        log.error(" ===> Health checker: {} is down", serviceAndInstance);
                        final int indexOf = serviceAndInstance.indexOf("@");
                        final String service = serviceAndInstance.substring(0, indexOf);
                        final InstanceMeta instance = InstanceMeta.from(serviceAndInstance.substring(indexOf));
                        registryService.unregister(service, instance);
                        KNRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                    }
                });
            }, 10, 30, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executor.shutdown();
    }
}
