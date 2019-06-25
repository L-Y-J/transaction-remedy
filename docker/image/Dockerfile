FROM mysql/mysql-server:5.6

MAINTAINER liyongjie <li646558434@hotmail.com>
LABEL Description="Docker: mysql + script + config for canal" Vendor="liyongjie" Version="1.0"

ENV MYSQL_DATABASE mysql

COPY my.cnf /etc
COPY init.sql /docker-entrypoint-initdb.d
COPY test.sh /docker-entrypoint-initdb.d
