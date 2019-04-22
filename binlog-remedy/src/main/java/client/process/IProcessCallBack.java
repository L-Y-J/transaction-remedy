package client.process;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * @author liyongjie
 */
public interface IProcessCallBack {

    String GLOBAL_TRANSACTION_PREFIX = "/**GLOBAL TRANSACTION ID:";
    String GLOBAL_TRANSACTION_POSTFIX = "**/";

    void startTransaction();

    void endTransaction();

    void saveSql(final String sql);

    void insertData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData);

    void deleteData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData);

    void updateData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData);

    default String extractGlobalTransaction(final String sql) {
        return StringUtils.substringBetween(sql, GLOBAL_TRANSACTION_PREFIX, GLOBAL_TRANSACTION_POSTFIX);
    }

    default boolean isGlobalTransaction(final String sql) {
        return sql != null && sql.startsWith(GLOBAL_TRANSACTION_PREFIX);
    }
}
