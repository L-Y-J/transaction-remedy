#!/bin/bash

cmd="docker run -d -p 3306:3306 --name mysql4canal -e MYSQL_ROOT_PASSWORD=123456 mysql4canal:1.0"
echo $cmd
eval $cmd
