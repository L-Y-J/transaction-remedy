package client.process;

import java.util.LinkedList;
import java.util.List;

import com.alibaba.otter.canal.protocol.CanalEntry;

import client.content.ProcessStatusEnum;
import base.constant.GlobalConstants;
import base.domain.TableRowDesc;
import base.domain.TransactionLog;
import client.storage.ITransactionStorage;
import client.storage.TransactionStorageFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liyongjie
 */
@Slf4j
public class SimpleProcessCallBackImpl implements IProcessCallBack {

    private static SimpleProcessCallBackImpl INSTANCE = new SimpleProcessCallBackImpl();

    private ProcessStatusEnum processStatusEnum = ProcessStatusEnum.WAIT_START;

    private final ITransactionStorage transactionStorage;

    private List<TransactionLog> logBuffer;

    private TransactionLog currentSqlLog;

    private SimpleProcessCallBackImpl() {
        transactionStorage = TransactionStorageFactory.getTransactionStorage();
        logBuffer = new LinkedList<>();
    }

    static SimpleProcessCallBackImpl getInstance() {
        return INSTANCE;
    }

    private void flushLog() {
        transactionStorage.saveTransactionLog(logBuffer);
    }

    @Override
    public void startTransaction() {
        processStatusEnum = ProcessStatusEnum.TRANSACTION_START;
        currentSqlLog = new TransactionLog();
        logBuffer.clear();
    }

    @Override
    public void endTransaction() {
        if (processStatusEnum == ProcessStatusEnum.DATA_INSERT
                || processStatusEnum == ProcessStatusEnum.DATA_UPDATE
                || processStatusEnum == ProcessStatusEnum.DATA_DELETE) {
            if (currentSqlLog.hasEffectiveData()) {
                logBuffer.add(currentSqlLog);
            }
            flushLog();
            processStatusEnum = ProcessStatusEnum.TRANSACTION_END;
        }
    }

    @Override
    public void saveSql(final String sql) {
        if (processStatusEnum == ProcessStatusEnum.TRANSACTION_START) {

        } else if (processStatusEnum == ProcessStatusEnum.DATA_INSERT
                || processStatusEnum == ProcessStatusEnum.DATA_UPDATE
                || processStatusEnum == ProcessStatusEnum.DATA_DELETE) {
            if (currentSqlLog.hasEffectiveData()) {
                logBuffer.add(currentSqlLog);
            }
            currentSqlLog = new TransactionLog();

        } else {
            log.info("non local transaction sql, [{}]", sql);
            return;
        }
        if (isGlobalTransaction(sql)) {
            currentSqlLog.setGlobalTransaction(extractGlobalTransaction(sql));
            currentSqlLog.setSql(sql);
            processStatusEnum = ProcessStatusEnum.SQL_STATEMENT;
        }
    }

    @Override
    public void insertData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData) {
        if (processStatusEnum == ProcessStatusEnum.SQL_STATEMENT) {
            currentSqlLog.setDmlType(GlobalConstants.INSERT);
            buildSqlLog(schemaName, tableName, gtid, rowData);
            processStatusEnum = ProcessStatusEnum.DATA_INSERT;
        } else {
            log.info("non transaction insert data, schemaName = [{}], tableName = [{}], gtid = [{}]", schemaName, tableName, gtid);
        }
    }

    @Override
    public void deleteData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData) {
        if (processStatusEnum == ProcessStatusEnum.SQL_STATEMENT) {
            currentSqlLog.setDmlType(GlobalConstants.DELETE);
            buildSqlLog(schemaName, tableName, gtid, rowData);
            processStatusEnum = ProcessStatusEnum.DATA_DELETE;
        } else {
            log.info("non transaction delete data, schemaName = [{}], tableName = [{}], gtid = [{}]", schemaName, tableName, gtid);
        }
    }

    @Override
    public void updateData(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData) {
        if (processStatusEnum == ProcessStatusEnum.SQL_STATEMENT) {
            currentSqlLog.setDmlType(GlobalConstants.UPDATE);
            buildSqlLog(schemaName, tableName, gtid, rowData);
            processStatusEnum = ProcessStatusEnum.DATA_UPDATE;
        } else {
            log.info("non transaction update data, schemaName = [{}], tableName = [{}], gtid = [{}]", schemaName, tableName, gtid);
        }
    }

    private void buildSqlLog(final String schemaName, final String tableName, final String gtid, final CanalEntry.RowData rowData) {
        currentSqlLog.setSchemaName(schemaName);
        currentSqlLog.setTableName(tableName);
        currentSqlLog.setGtid(gtid);
        for (final CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            final TableRowDesc beforeDataDesc = TableRowDesc.builder()
                    .name(column.getName())
                    .value(column.getValue())
                    .type(column.getSqlType())
                    .mysqlType(column.getMysqlType())
                    .isKey(column.getIsKey())
                    .build();
            currentSqlLog.addBeforeDataDesc(beforeDataDesc);
            if (column.getIsKey()) {
                currentSqlLog.setKeyName(column.getName());
                currentSqlLog.setKeyValue(column.getValue());
            }
        }
        for (final CanalEntry.Column column : rowData.getAfterColumnsList()) {
            final TableRowDesc afterDataDesc = TableRowDesc.builder()
                    .name(column.getName())
                    .value(column.getValue())
                    .type(column.getSqlType())
                    .mysqlType(column.getMysqlType())
                    .isKey(column.getIsKey())
                    .build();
            currentSqlLog.addAfterDataDesc(afterDataDesc);
            if (column.getIsKey()) {
                currentSqlLog.setKeyName(column.getName());
                currentSqlLog.setKeyValue(column.getValue());
            }
        }
    }
}
