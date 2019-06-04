package remedy.rollback.jdbc;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import base.constant.GlobalConstants;
import base.domain.TableRowDesc;
import base.domain.TransactionLog;
import base.jdbc.JdbcTemplateSelector;
import base.prop.PropHolder;
import lombok.extern.slf4j.Slf4j;
import remedy.rollback.IRollbackProcessor;

/**
 * @author liyongjie
 */
@Slf4j
public class JdbcRollbackProcessor implements IRollbackProcessor {

    private DataSourceTransactionManager transactionManager = null;
    private TransactionStatus status = null;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 50, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new BasicThreadFactory.Builder().namingPattern("JdbcRollbackProcessor-thread-%d").build());


    @Override
    public boolean rollbackByTransactionId(final String transactionId) {
        executor.submit(() -> {
            final List<TransactionLog> transactionLogs = loadTransactionLogByTransactionId(transactionId);
            startJdbcTransaction();
            for (final TransactionLog transactionLog : transactionLogs) {
                String rollbackSql = generateRollbackSql(transactionLog);
                if (!persistRollbackSql(transactionLog.getId(), rollbackSql)) {
                    rollbackJdbcTransaction();
                    return;
                }
                if (!executeRollBackSql(transactionLog.getSchemaName(), rollbackSql)) {
                    rollbackJdbcTransaction();
                    return;
                }
            }
            commitJdbcTransaction();
        });
        return true;
    }

    @Override
    public boolean rollbackByMysqlGtid(final String gtid) {
        executor.submit(() -> {

        });
        return true;
    }

    private String generateRollbackSql(TransactionLog transactionLog) {
        if (GlobalConstants.INSERT.equals(transactionLog.getDmlType())) {
            String tableName = transactionLog.getTableName();
            String primaryKeyName = transactionLog.getKeyName();
            String primaryKeyValue = transactionLog.getKeyValue();;
            for (TableRowDesc row : transactionLog.getAfterDataDescList()) {
                if (row.getName().equals(primaryKeyName)) {
                    JDBCType primaryType = JDBCType.valueOf(row.getType());
					primaryKeyValue = wrapValueWithQuotes(primaryType, primaryKeyValue);
                }
            }
            return String.format(SqlStatementConstants.DELETE_STATEMENT, tableName, primaryKeyName, primaryKeyValue);
        } else if (GlobalConstants.DELETE.equals(transactionLog.getDmlType())) {
            String tableName = transactionLog.getTableName();
            StringBuilder columnNamesBuilder = new StringBuilder();
            StringBuilder columnValuesBuilder = new StringBuilder();
            for (final TableRowDesc row : transactionLog.getBeforeDataDescList()) {
                columnNamesBuilder.append(row.getName()).append(",");
                String columnValue = wrapValueWithQuotes(JDBCType.valueOf(row.getType()), row.getValue());
                columnValuesBuilder.append(columnValue).append(",");
            }
            String columnNames = StringUtils.removeEnd(columnNamesBuilder.toString(), ",");
            String columnValues = StringUtils.removeEnd(columnValuesBuilder.toString(), ",");
            return String.format(SqlStatementConstants.INSERT_STATEMENT, tableName, columnNames, columnValues);
        } else if (GlobalConstants.UPDATE.equals(transactionLog.getDmlType())) {
            String tableName = transactionLog.getTableName();
            String primaryKeyName = transactionLog.getKeyName();
            String primaryKeyValue = transactionLog.getKeyValue();
            StringBuilder updatedColumnsBuilder = new StringBuilder();
            final List<TableRowDesc> beforeDataDescList = transactionLog.getBeforeDataDescList();
            final List<TableRowDesc> afterDataDescList = transactionLog.getAfterDataDescList();
            for (int i = 0; i < beforeDataDescList.size(); i++) {
                final TableRowDesc before = beforeDataDescList.get(i);
                final TableRowDesc after = afterDataDescList.get(i);
                if (!StringUtils.equals(before.getValue(), after.getValue())) {
                    final String beforeName = before.getName();
                    String beforeValue = wrapValueWithQuotes(JDBCType.valueOf(before.getType()), before.getValue());
                    updatedColumnsBuilder.append(beforeName).append("=").append(beforeValue).append(",");
                }
            }
            String updatedColumns = StringUtils.removeEnd(updatedColumnsBuilder.toString(), ",");
            return String.format(SqlStatementConstants.UPDATE_STATEMENT, tableName, updatedColumns, primaryKeyName, primaryKeyValue);
        }
        return null;
    }

    private String wrapValueWithQuotes(JDBCType type, String value) {
        switch(type) {
            case BIT:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case BIGINT:
            case FLOAT:
            case REAL:
            case DOUBLE:
            case NUMERIC:
            case DECIMAL:
                break;
            default:
                value = "'"  + value +"'";
                break;
        }
        return value;
    }

    private boolean persistRollbackSql(Long id, String rollbackSql) {
        try {
            final NamedParameterJdbcTemplate jdbcTemplate = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
            final Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            params.put("rollbackSql", rollbackSql);
            jdbcTemplate.update("insert into transaction_log_rollback (id,rollback_sql) values (:id,:rollbackSql)", params);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private boolean executeRollBackSql(String schema, String rollbackSql) {
        final NamedParameterJdbcTemplate jdbcTemplate = JdbcTemplateSelector.selectJdbcTemplateBySchema(schema);
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update(rollbackSql, new HashMap<>());
                return true;
            } catch (DataAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }

    private void startJdbcTransaction() {
        transactionManager = JdbcTemplateSelector.selectTransactionManager(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        status = transactionManager.getTransaction(txDef);
    }

    private void commitJdbcTransaction() {
        transactionManager.commit(status);
    }

    private void rollbackJdbcTransaction() {
        transactionManager.rollback(status);
    }

    private List<TransactionLog> loadTransactionLogByTransactionId(final String transactionId) {
        final NamedParameterJdbcTemplate jdbcTemplate = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        final String sql = "select * from transaction_log where global_transaction = :transactionId";
        final Map<String, Object> params = new HashMap<>();
        params.put("transactionId", transactionId);
        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<TransactionLog>(TransactionLog.class));
    }

    private List<TransactionLog> loadTransactionLogByGtid(final String gtid) {
        final NamedParameterJdbcTemplate jdbcTemplate = JdbcTemplateSelector.selectJdbcTemplateBySchema(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA));
        final String sql = "select * from transaction_log where gtid = :gtid";
        final Map<String, Object> params = new HashMap<>();
        params.put("gtid", gtid);
        return jdbcTemplate.queryForList(sql, params, TransactionLog.class);
    }
}
