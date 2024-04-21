package cn.lokn.knregistry.model;

import lombok.Data;

/**
 * @description: Registry server instance
 * @author: lokn
 * @date: 2024/04/20 23:40
 */
@Data
public class Server {

    private String url;
    private boolean status;
    private boolean leader;
    private long version;

}
