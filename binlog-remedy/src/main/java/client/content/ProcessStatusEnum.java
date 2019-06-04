package client.content;

/**
 * @author liyongjie
 */

public enum ProcessStatusEnum {
    /**
     * 等待开始
     */
    WAIT_START,
    /**
     * 事务开始日志
     */
    TRANSACTION_START,
    /**
     * 事务结束日志
     */
    TRANSACTION_END,
    /**
     * SQL语句日志
     */
    SQL_STATEMENT,
    /**
     * INSERT日志
     */
    DATA_INSERT,
    /**
     * UPDATE日志
     */
    DATA_UPDATE,
    /**
     * DELETE日志
     */
    DATA_DELETE
}
