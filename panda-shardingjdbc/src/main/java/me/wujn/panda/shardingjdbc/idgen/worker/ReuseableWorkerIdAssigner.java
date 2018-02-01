/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker;

import me.wujn.panda.shardingjdbc.idgen.exception.WorkerIdAssignException;
import me.wujn.panda.shardingjdbc.idgen.utils.NetUtils;
import me.wujn.panda.shardingjdbc.idgen.worker.repos.WorkerNodeRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * if a worker node only deployed one application, the application name can be empty.
     * otherwise a worker node deployed multi-application,the app name is necessary.
     */
    private String appName;

    @Override
    public long assignWorkerId() {
        String ipAddress = NetUtils.getLocalAddress();
        if (StringUtils.isBlank(ipAddress)) {
            throw new WorkerIdAssignException("ipaddress is not valid " + ipAddress);
        }
        return workerNodeRepository.assignWorkerId(ipAddress, appName);
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
