package client.content;

/**
 * @author liyongjie
 */

public enum ProcessStatusEnum {
    WAIT_START,
    TRANSACTION_START,
    TRANSACTION_END,
    SQL_STATEMENT,
    DATA_INSERT,
    DATA_UPDATE,
    DATA_DELETE
}
