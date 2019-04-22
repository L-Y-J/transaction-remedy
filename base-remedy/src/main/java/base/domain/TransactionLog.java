package base.domain;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

import lombok.Data;

/**
 * @author liyongjie
 */
@Data
public class TransactionLog {
    private Long id;
    private String sql;
    private String globalTransaction;
    private String gtid;
    private String schemaName;
    private String tableName;
    private String dmlType;
    private String keyName;
    private String keyValue;
    private List<TableRowDesc> beforeDataDescList;
    private String beforeData;
    private List<TableRowDesc> afterDataDescList;
    private String afterData;

    public String getBeforeData() {
        return JSON.toJSONString(beforeDataDescList);
    }

    public void setBeforeData(final String beforeData) {
        this.beforeData = beforeData;
        beforeDataDescList = JSON.parseArray(beforeData, TableRowDesc.class);
    }

    public String getAfterData() {
        return JSON.toJSONString(afterDataDescList);
    }

    public void setAfterData(final String afterData) {
        this.afterData = afterData;
        afterDataDescList = JSON.parseArray(afterData, TableRowDesc.class);
    }

    public boolean hasEffectiveData() {
        return sql != null && globalTransaction != null && schemaName != null && tableName != null && dmlType != null;
    }

    public void addBeforeDataDesc(TableRowDesc beforeDataDesc) {
        if (beforeDataDescList == null) {
            beforeDataDescList = new ArrayList<>();
        }
        beforeDataDescList.add(beforeDataDesc);
    }

    public void addAfterDataDesc(TableRowDesc afterDataDesc) {
        if (afterDataDescList == null) {
            afterDataDescList = new ArrayList<>();
        }
        afterDataDescList.add(afterDataDesc);
    }
}
