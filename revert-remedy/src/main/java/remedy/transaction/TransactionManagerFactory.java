package remedy.transaction;

import org.apache.commons.lang3.StringUtils;

import base.prop.PropHolder;
import lombok.experimental.UtilityClass;
import remedy.transaction.jdbc.JdbcTransactionManager;

/**
 * @author liyongjie
 */
@UtilityClass
public class TransactionManagerFactory {

    public static ITransactionManager getTransactionManager() {
        if (StringUtils.isNotEmpty(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA))) {
            return JdbcTransactionManager.getInstance();
        }
        throw new RuntimeException("Have no available transaction manager");
    }
}
