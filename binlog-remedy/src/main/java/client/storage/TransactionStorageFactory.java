package client.storage;

import org.apache.commons.lang3.StringUtils;

import base.prop.PropHolder;
import lombok.experimental.UtilityClass;

/**
 * @author liyongjie
 */
@UtilityClass
public class TransactionStorageFactory {

    public static ITransactionStorage getTransactionStorage() {
        if (StringUtils.isNotEmpty(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA))) {
            return new JdbcTransactionStorage();
        }
        throw new RuntimeException("Have no available transaction storage");
    }
}
