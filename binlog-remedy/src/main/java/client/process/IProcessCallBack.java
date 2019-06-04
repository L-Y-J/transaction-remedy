package client.process;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * @author liyongjie
 */
public interface IProcessCallBack {

    String GLOBAL_TRANSACTION_PREFIX = "/**GLOBAL TRANSACTION ID:";
    String GLOBAL_TRANSACTION_POSTFIX = "**/";

    /**
     * 事务开始日志回调
     */
    void startTransaction();

    /**
     * 事务结束日志回调
     */
    void endTransaction();

    /**
     * 保存SQL语句回调
     * @param sql
     */
    void saveSql(final String sql);

    /**
     * INSERT日志回调
     * @param schemaName
     * @param tableName
     * @param gtid
     * @param rowData
     */
    void insertData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData);

    /**
     * DELETE日志回调
     * @param schemaName
     * @param tableName
     * @param gtid
     * @param rowData
     */
    void deleteData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData);

    /**
     * UPDATE日志回调
     * @param schemaName
     * @param tableName
     * @param gtid
     * @param rowData
     */
    void updateData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData);

    /**
     * 抽取事务ID
     * @param sql
     * @return
     */
    default String extractGlobalTransaction(final String sql) {
        return StringUtils.substringBetween(sql, GLOBAL_TRANSACTION_PREFIX, GLOBAL_TRANSACTION_POSTFIX);
    }

    /**
     * 是否携带全局事务ID
     * @param sql
     * @return
     */
    default boolean isGlobalTransaction(final String sql) {
        return sql != null && sql.startsWith(GLOBAL_TRANSACTION_PREFIX);
    }
}
