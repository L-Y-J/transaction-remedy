package base.prop;

import java.io.IOException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * @author liyongjie
 */
@Slf4j
public class PropHolder {

    static public Properties prop;

    static public String SQL_LOG_SCHEMA = "sql.log.schema";

    static public String CLIENT_FILTER = "client.filter";
    static public String SERVER_DESTINATION = "server.destination";
    static public String SERVER_IP = "server.ip";
    static public String SERVER_PORT = "server.port";
    static public String SERVER_USERNAME = "server.username";
    static public String SERVER_PASSWORD = "server.password";

    static {
        prop = new Properties();
        try {
            prop.load(PropHolder.class.getResourceAsStream("/client.properties"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
