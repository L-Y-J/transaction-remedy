package remedy.transaction.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import base.jdbc.JdbcTemplateSelector;
import base.prop.PropHolder;
import remedy.rollback.jdbc.JdbcRollbackProcessor;
import remedy.transaction.ITransactionManager;

/**
 * @author liyongjie
 */
public class JdbcTransactionManager implements ITransactionManager {

    private static ITransactionManager INSTANCE = new JdbcTransactionManager();

    private JdbcTransactionManager() {
    }

    public static ITransactionManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean createTransaction(final String transactionId) {
        final NamedParameterJdbcTemplate jdbcTemplate = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        final Map<String, Object> params = new HashMap<>();
        params.put("transactionId", transactionId);
        jdbcTemplate.update("insert into global_transaction (global_transaction, transaction_status, transaction_start) values (:transactionId,'START',now())", params);
        return true;
    }

    @Override
    public boolean submitTransaction(final String transactionId) {
        final NamedParameterJdbcTemplate jdbcTemplate = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        final Map<String, Object> params = new HashMap<>();
        params.put("transactionId", transactionId);
        jdbcTemplate.update("update global_transaction set transaction_status='COMMIT',transaction_commit=now() where global_transaction=:transactionId", params);
        return true;
    }

    @Override
    public boolean rollbackTransaction(final String transactionId) {
        final NamedParameterJdbcTemplate jdbcTemplate = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        final Map<String, Object> params = new HashMap<>();
        params.put("transactionId", transactionId);
        jdbcTemplate.update("update global_transaction set transaction_status='ROLLBACK',transaction_rollback=now() where global_transaction=:transactionId", params);
        new JdbcRollbackProcessor().rollbackByTransactionId(transactionId);
        return true;
    }
}
