
package remedy.rollback.jdbc;

/**
 * @author liyongjie
 */
public class SqlStatementConstants {

    public static String DELETE_STATEMENT = "delete from %s where %s = %s";

    public static String INSERT_STATEMENT = "insert into %s(%s) values(%s)";

    public static String UPDATE_STATEMENT = "update %s set %s where %s = %s";

}