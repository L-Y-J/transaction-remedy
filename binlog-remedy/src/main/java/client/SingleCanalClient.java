package client;

import java.net.InetSocketAddress;
import java.util.List;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;

import base.prop.PropHolder;
import client.process.IProcessCallBack;
import client.process.ProcessCallBackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liyongjie
 */

@Slf4j
public class SingleCanalClient extends AbstractCanalClient {

    private IProcessCallBack processCallBack;

    private SingleCanalClient(String destination, CanalConnector connector, String filter) {
        super(destination, connector, filter);
        processCallBack = ProcessCallBackFactory.getProcessCallBack();
    }

    @Override
    protected void businessProcess(final List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN) {
                    processCallBack.startTransaction();
                } else {
                    processCallBack.endTransaction();
                }
                continue;
            }

            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                CanalEntry.RowChange rowChage;
                try {
                    rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                } catch (Exception e) {
                    throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
                }

                CanalEntry.EventType eventType = rowChage.getEventType();

                if (eventType == CanalEntry.EventType.QUERY || rowChage.getIsDdl()) {
                    processCallBack.saveSql(rowChage.getSql());
                    continue;
                }

                for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                    if (eventType == CanalEntry.EventType.DELETE) {
                        processCallBack.deleteData(entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                                entry.getHeader().getGtid(), rowData);
                    } else if (eventType == CanalEntry.EventType.INSERT) {
                        processCallBack.insertData(entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                                entry.getHeader().getGtid(), rowData);
                    } else {
                        processCallBack.updateData(entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                                entry.getHeader().getGtid(), rowData);
                    }
                }
            }
        }
    }


    public static void main(String[] args) {
        // 根据ip，直接创建链接，无HA的功能
        String destination = PropHolder.prop.getProperty(PropHolder.SERVER_DESTINATION);
        String ip = PropHolder.prop.getProperty(PropHolder.SERVER_IP);
        int port = Integer.valueOf(PropHolder.prop.getProperty(PropHolder.SERVER_PORT));
        String username = PropHolder.prop.getProperty(PropHolder.SERVER_USERNAME);
        String password = PropHolder.prop.getProperty(PropHolder.SERVER_PASSWORD);
        String filter = PropHolder.prop.getProperty(PropHolder.CLIENT_FILTER);
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(ip, port), destination, username, password);
        final SingleCanalClient clientTest = new SingleCanalClient(destination, connector, filter);
        clientTest.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("## stop the canal client");
                clientTest.stop();
            } catch (Throwable e) {
                log.warn("##something goes wrong when stopping canal:", e);
            } finally {
                log.info("## canal client is down.");
            }
        }));
    }

}
