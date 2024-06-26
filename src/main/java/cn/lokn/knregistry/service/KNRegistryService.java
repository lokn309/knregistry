package cn.lokn.knregistry.service;

import cn.lokn.knregistry.clustre.SnapShot;
import cn.lokn.knregistry.http.HttpInvoker;
import cn.lokn.knregistry.http.OkHttpInvoker;
import cn.lokn.knregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @description: Default implements of RegistryService
 * @author: lokn
 * @date: 2024/04/13 19:33
 */
@Slf4j
public class KNRegistryService implements RegistryService {

    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    public final static AtomicLong VERSION = new AtomicLong(0L);

    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
        final List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas != null && !metas.isEmpty()) {
            if (metas.contains(instance)) {
                log.info(" ===> instance {} already registered", instance.toUrl());
                instance.setStatus(true);
                return instance;
            }
        }

        log.info(" ===> register instance {} ", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        final List<InstanceMeta> metas = REGISTRY.get(service);
        if (metas == null || metas.isEmpty()) {
            return null;
        }
        log.info(" ===> unregister instance {} ", instance.toUrl());
        metas.removeIf(x -> x.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.get(service);
    }

    @Override
    public long renew(InstanceMeta instance, String... services) {
        final long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(Collectors.toMap(x -> x, VERSIONS::get, (a, b) -> a));
    }

    public static synchronized SnapShot snapShot() {
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new HashMap<>(VERSIONS);
        Map<String, Long> timestamps = new HashMap<>(TIMESTAMPS);
        return new SnapShot(registry, versions, timestamps, VERSION.get());
    }

    public static synchronized long restore(SnapShot snapShot) {
        REGISTRY.clear();
        REGISTRY.addAll(snapShot.getRegistry());
        VERSIONS.clear();
        VERSIONS.putAll(snapShot.getVersions());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapShot.getTimestamps());
        VERSION.set(snapShot.getVersion());

        return snapShot.getVersion();
    }



}
