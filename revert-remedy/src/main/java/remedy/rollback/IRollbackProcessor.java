package remedy.rollback;

/**
 * @author liyongjie
 */
public interface IRollbackProcessor {

    boolean rollbackByTransactionId(String transactionId);

    boolean rollbackByMysqlGtid(String gtid);

}
