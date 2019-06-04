package base.domain;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * @author liyongjie
 */
@Builder
@Data
public class TableRowDesc {
    private String name;
    private String value;
    private int type;
    private String mysqlType;
    private boolean isKey;

    @Tolerate
    public TableRowDesc() {
    }
}
