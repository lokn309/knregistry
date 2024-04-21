package cn.lokn.knregistry.clustre;

import cn.lokn.knregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * @description:
 * @author: lokn
 * @date: 2024/04/21 20:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnapShot {

    LinkedMultiValueMap<String, InstanceMeta> registry;
    Map<String, Long> versions;
    Map<String, Long> timestamps;
    long version;

}
