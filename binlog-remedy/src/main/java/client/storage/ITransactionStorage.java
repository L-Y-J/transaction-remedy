
package client.storage;

import java.util.List;

import base.domain.TransactionLog;

/**
 * @author liyongjie
 */
public interface ITransactionStorage {

    /**
     * 全局事务是否已经提交
     *
     * @param globalTransaction 全局事务ID
     * @return true/false
     */
    boolean globalTransactionIsCommit(String globalTransaction);

    /**
     * 本地事务是否已经记录
     *
     * @param gtid mysql GTID
     * @return true/false
     */
    boolean localTransactionIsStored(String gtid);

    /**
     * 存储事务日志
     *
     * @param transactionLogList 事务日志
     */
    void saveTransactionLog(List<TransactionLog> transactionLogList);

}