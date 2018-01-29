/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker;

import me.wujn.panda.shardingjdbc.idgen.repository.WorkerNode;
import me.wujn.panda.shardingjdbc.idgen.repository.WorkerNodeRepository;
import me.wujn.panda.shardingjdbc.idgen.utils.NetUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author wujn
 * @version $Id ReuseableWorkerIdAssigner.java, v 0.1 2018-01-24 10:30 wujn Exp $$
 */
@Service
public class ReuseableWorkerIdAssigner implements WorkerIdAssigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReuseableWorkerIdAssigner.class);

    @Autowired
    private WorkerNodeRepository workerNodeRepository;

    /**
     * enable the file cache when get worker id from zookeeper
     */
    private Boolean enableFileCache;

    /**
     * config the file path for the local worker id
     */
    private String fileCachePath;

    /**
     * default worker id
     */
    private static final Long DEFAULT_WORKER_ID = 1L;
    /**
     * if a worker node only deployed one application, the application name can be empty.
     * otherwise a worker node deployed multi-application,the app name is necessary.
     */
    private String appName;

    @Override
    public long assignWorkerId() {
        String ipAddress = NetUtils.getLocalAddress();
        try {
            WorkerNode workerNode = workerNodeRepository.get(ipAddress, appName);
            if (workerNode == null) {
                //insert a new worker node
                workerNode = new WorkerNode();
                workerNode.setAppName(appName);
                workerNode.setCreated(new Date());
                workerNode.setModified(new Date());
                workerNode.setHostName(ipAddress);
                workerNode.setWorkerId(DEFAULT_WORKER_ID);
                workerNodeRepository.insert(workerNode);
            }
            return workerNode.getWorkerId();
        } catch (Exception ex) {
            LOGGER.error("assign worker id error : " + ex.getMessage(), ex);
            return 0L;
        }
    }

    /**
     * Setter method for property <tt>appName</tt>.
     *
     * @param appName value to be assigned to property appName
     */
    public void setAppName(String appName) {
        if (StringUtils.isBlank(appName)) {
            this.appName = "default-app";
        } else {
            this.appName = appName;
        }
    }
}
