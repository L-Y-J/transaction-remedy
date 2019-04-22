package remedy.transaction;

/**
 * @author liyongjie
 */
public interface ITransactionManager {

    boolean createTransaction(String transactionId);

    boolean submitTransaction(String transactionId);

    boolean rollbackTransaction(String transactionId);
}
