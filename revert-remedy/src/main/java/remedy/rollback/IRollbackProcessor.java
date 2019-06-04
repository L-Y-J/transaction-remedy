package remedy.rollback;

/**
 * @author liyongjie
 */
public interface IRollbackProcessor {

    /**
     * 通过事务ID回滚数据
     * @param transactionId
     * @return
     */
    boolean rollbackByTransactionId(String transactionId);

    /**
     * 通过GTID回滚数据
     * @param gtid
     * @return
     */
    boolean rollbackByMysqlGtid(String gtid);

}
