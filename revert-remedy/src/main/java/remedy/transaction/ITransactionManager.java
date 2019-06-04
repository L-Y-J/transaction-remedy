package remedy.transaction;

/**
 * @author liyongjie
 */
public interface ITransactionManager {

    /**
     * 创建全局事务
     * @param transactionId
     * @return
     */
    boolean createTransaction(String transactionId);

    /**
     * 提交全局事务
     * @param transactionId
     * @return
     */
    boolean submitTransaction(String transactionId);

    /**
     * 回滚全局事务
     * @param transactionId
     * @return
     */
    boolean rollbackTransaction(String transactionId);
}
