package client.process;

import org.apache.commons.lang3.StringUtils;

import base.prop.PropHolder;
import lombok.experimental.UtilityClass;

/**
 * @author liyongjie
 */
@UtilityClass
public class ProcessCallBackFactory {

    public static IProcessCallBack getProcessCallBack() {
        if (StringUtils.isNotEmpty(PropHolder.prop.getProperty(PropHolder.SQL_LOG_SCHEMA))) {
            return SimpleProcessCallBackImpl.getInstance();
        }
        throw new RuntimeException("Have no available transaction storage");
    }
}
