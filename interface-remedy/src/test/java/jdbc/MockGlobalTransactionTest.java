package jdbc;

import java.util.HashMap;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import base.jdbc.JdbcTemplateSelector;

public class MockGlobalTransactionTest {

    public static void main(String[] args) {
        final NamedParameterJdbcTemplate template = JdbcTemplateSelector.selectJdbcTemplateBySchema("test");
        final DataSourceTransactionManager transactionManager = JdbcTemplateSelector.selectTransactionManager("test");
        TransactionDefinition txDef = new DefaultTransactionDefinition();
        final TransactionStatus status = transactionManager.getTransaction(txDef);
        final HashMap<String, Object>[] params = new HashMap[2];
        params[0] = new HashMap<>();
        params[0].put("id", Integer.valueOf(6));
        params[1] = new HashMap<>();
        params[1].put("id", Integer.valueOf(7));
        //template.batchUpdate("/**GLOBAL TRANSACTION ID:123**/delete from mytest where id = :id", params);
        template.update("/**GLOBAL TRANSACTION ID:123**/insert into mytest(name) values ('qwe2')", new HashMap<>());
        transactionManager.commit(status);
    }
}
