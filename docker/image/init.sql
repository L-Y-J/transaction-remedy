CREATE USER canal IDENTIFIED BY 'canal';
GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;

create table test.mytest(
    id int unsigned primary key auto_increment,
    name varchar(50) null
);

create schema sql_data;

create table sql_data.global_transaction
(
    global_transaction   varchar(50) primary key,
    transaction_status   varchar(10) not null comment '事务状态(START,COMMIT,ROLLBACK)',
    transaction_start    datetime    null comment '启动事务时间',
    transaction_commit   datetime    null comment '提交事务时间',
    transaction_rollback datetime    null comment '回滚事务时间'
);

create table sql_data.transaction_log
(
    id                 int unsigned primary key auto_increment,
    global_transaction varchar(50) not null,
    gtid               varchar(50) null,
    schema_name        varchar(50) not null,
    table_name         varchar(50) not null,
    dml_type           varchar(10) not null comment 'INSERT/UPDATE/DELETE',
    key_name           varchar(50) null,
    key_value          varchar(50) null,
    before_data        longtext    null,
    after_data         longtext    null
);


create table sql_data.gtransaction_log_rollback
(
    id           int unsigned primary key,
    rollback_sql varchar(1000) not null
);

