package client.storage;

import java.util.HashMap;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import base.jdbc.JdbcTemplateSelector;
import base.prop.PropHolder;
import base.domain.TransactionLog;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liyongjie
 */
@Slf4j
public class JdbcTransactionStorage implements ITransactionStorage {

    @Override
    public boolean globalTransactionIsCommit(final String globalTransaction) {
        final NamedParameterJdbcTemplate template = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        final String sql = "select transaction_status from global_transaction where global_transaction = :globalTransaction";
        final HashMap<String, Object> params = new HashMap<>();
        params.put("globalTransaction", globalTransaction);
        String status;
        try {
            status = template.queryForObject(sql, params, String.class);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            // skip this error log
            return true;
        }
        return "COMMIT".equals(status);
    }

    @Override
    public boolean localTransactionIsStored(final String gtid) {
        final NamedParameterJdbcTemplate template = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        String sql = "select gtid from transaction_log where gtid = :gtid";
        final HashMap<String, Object> params = new HashMap<>();
        params.put("gtid", gtid);
        String savedGtid;
        try {
            savedGtid = template.queryForObject(sql, params, String.class);
        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            // skip this error log
            return true;
        }
        return savedGtid != null;
    }

    @Override
    public void saveTransactionLog(final List<TransactionLog> transactionLogList) {
        if (transactionLogList.isEmpty()) {
            return;
        }

        final NamedParameterJdbcTemplate template = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));

        String globalTransaction = transactionLogList.get(0).getGlobalTransaction();
        if (globalTransactionIsCommit(globalTransaction)) {
            log.info("committed global transaction:[{}]", globalTransaction);
            return;
        }
        String gtid = transactionLogList.get(0).getGtid();
        if (localTransactionIsStored(gtid)) {
            log.info("has saved GTID:[{}]", gtid);
            return;
        }

        final String sql = "insert into transaction_log " +
                "(global_transaction, gtid, schema_name, table_name, dml_type, key_name, key_value, before_data, after_data)" +
                "values (:globalTransaction,:gtid,:schemaName,:tableName,:dmlType,:keyName,:keyValue,:beforeData,:afterData)";
        final SqlParameterSource[] parameterSources = transactionLogList
                .stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);
        template.batchUpdate(sql, parameterSources);
    }
}
