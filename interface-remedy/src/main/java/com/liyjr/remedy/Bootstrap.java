package com.liyjr.remedy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import client.SingleCanalClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liyongjie
 */
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
        log.info("------------------------- Interface-remedy started! -------------------------");
        SingleCanalClient.main(null);
        log.info("------------------------- binlog-remedy started! -------------------------");
    }

}
