package cn.lokn.knregistry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @description: Registry server instance
 * @author: lokn
 * @date: 2024/04/20 23:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"url"})
public class Server {

    private String url;
    private boolean status;
    private boolean leader;
    private long version;

}
