/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker;

import me.wujn.panda.shardingjdbc.idgen.utils.NetUtils;
import me.wujn.panda.shardingjdbc.idgen.utils.SystemClock;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author wujn
 * @version $Id ReuseableWorkerIdAssigner.java, v 0.1 2018-01-24 10:30 wujn Exp $$
 */
@Service
public class ReuseableWorkerIdAssigner implements WorkerIdAssigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReuseableWorkerIdAssigner.class);
    @Autowired
    private WorkerNodeRepository workerNodeRepository;

    @Override
    public long assignWorkerId() {
        try {
            String hostName = NetUtils.getLocalInetAddress().getHostName();
            String port = "0";//String.valueOf(SystemClock.millisClock().now() + RandomUtils.nextInt(0, 10000));
            WorkerNode workerNode = workerNodeRepository.get(hostName, port);
            if (workerNode == null) {
                //insert a worker node
                workerNode = new WorkerNode();
                workerNodeRepository.insert(workerNode);
            }
            return Long.valueOf(workerNode.getWorkerId());
        } catch (Exception ex) {
            LOGGER.error("worker id assign error", ex);
            return 0;
        }
    }

    @Override
    public void resetWorkerId() {

    }
}
