package com.liyjr.remedy.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import remedy.transaction.TransactionManagerFactory;

/**
 * @author liyongjie
 */
@RestController
@RequestMapping("transaction")
public class TransactionController {

    @PostMapping("/create")
    @ApiOperation(value = "创建事务")
    @ApiImplicitParam(name = "globalTransaction", value = "事务ID", required = true)
    public String createTransaction(@RequestParam String globalTransaction) {
        TransactionManagerFactory.getTransactionManager().createTransaction(globalTransaction);
        return "创建成功";
    }

    @PostMapping("/submit")
    @ApiOperation(value = "提交事务")
    @ApiImplicitParam(name = "globalTransaction", value = "事务ID", required = true)
    public String submitTransaction(@RequestParam String globalTransaction) {
        TransactionManagerFactory.getTransactionManager().submitTransaction(globalTransaction);
        return "提交成功";
    }

    @PostMapping("/rollback")
    @ApiOperation(value = "回滚事务")
    @ApiImplicitParam(name = "globalTransaction", value = "事务ID", required = true)
    public String rollbackTransaction(@RequestParam String globalTransaction) {
        TransactionManagerFactory.getTransactionManager().rollbackTransaction(globalTransaction);
        return "回滚成功";
    }
}
