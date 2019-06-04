package com.liyjr.remedy.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import remedy.transaction.TransactionManagerFactory;

/**
 * @author liyongjie
 */
@RestController
@RequestMapping("transaction")
@Api(value = "TransactionController", description = "事务接口")
public class TransactionController {

    @PostMapping("/create")
    @ApiOperation(value = "创建事务")
    public String createTransaction(@ApiParam(value = "事务ID", required = true) @RequestParam String globalTransaction) {
        TransactionManagerFactory.getTransactionManager().createTransaction(globalTransaction);
        return "创建成功";
    }

    @PostMapping("/submit")
    @ApiOperation(value = "提交事务")
    @ApiImplicitParam(name = "globalTransaction", value = "事务ID", required = true)
    public String submitTransaction(@ApiParam(value = "事务ID", required = true) @RequestParam String globalTransaction) {
        TransactionManagerFactory.getTransactionManager().submitTransaction(globalTransaction);
        return "提交成功";
    }

    @PostMapping("/rollback")
    @ApiOperation(value = "回滚事务")
    public String rollbackTransaction(@ApiParam(value = "事务ID", required = true) @RequestParam String globalTransaction) {
        TransactionManagerFactory.getTransactionManager().rollbackTransaction(globalTransaction);
        return "回滚成功";
    }
}
