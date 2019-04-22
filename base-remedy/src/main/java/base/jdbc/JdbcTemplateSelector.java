package base.jdbc;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;

import base.prop.PropHolder;
import lombok.experimental.UtilityClass;

/**
 * @author liyongjie
 */
@UtilityClass
public class JdbcTemplateSelector {

    static private ConcurrentHashMap<String, NamedParameterJdbcTemplate> templateMap = new ConcurrentHashMap<>();
    static private ConcurrentHashMap<String, DruidDataSource> dataSourceMap = new ConcurrentHashMap<>();
    static private ConcurrentHashMap<String, DataSourceTransactionManager> transactionManagerMap = new ConcurrentHashMap<>();

    static public NamedParameterJdbcTemplate selectJdbcTemplateBySchema(String schema) {
        return templateMap.computeIfAbsent(schema, sc -> {
            final DruidDataSource dataSource = getDataSource(schema);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return new NamedParameterJdbcTemplate(jdbcTemplate);
        });
    }

    static public DataSourceTransactionManager selectTransactionManager(String schema) {
        final DruidDataSource dataSource = getDataSource(schema);
        return transactionManagerMap.computeIfAbsent(schema, sc -> new DataSourceTransactionManager(dataSource));
    }

    static private DruidDataSource getDataSource(String schema) {
        return dataSourceMap.computeIfAbsent(schema, sc -> {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName(PropHolder.prop.getProperty(sc + ".druid.driver-class-name"));
            dataSource.setUrl(PropHolder.prop.getProperty(sc + ".druid.url"));
            dataSource.setUsername(PropHolder.prop.getProperty(sc + ".druid.username"));
            dataSource.setPassword(PropHolder.prop.getProperty(sc + ".druid.password"));
            return dataSource;
        });
    }
}
