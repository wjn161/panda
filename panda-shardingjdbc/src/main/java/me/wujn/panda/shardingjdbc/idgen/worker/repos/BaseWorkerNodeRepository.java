/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker.repos;

import me.wujn.panda.shardingjdbc.idgen.cache.WorkerIdFileCache;
import me.wujn.panda.shardingjdbc.idgen.exception.WorkerIdAssignException;
import me.wujn.panda.shardingjdbc.idgen.worker.WorkerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author wujn
 * @version $Id BaseWorkerNodeRepository.java, v 0.1 2018-02-01 14:46 wujn Exp $$
 */
public abstract class BaseWorkerNodeRepository implements WorkerNodeRepository {
    /**
     * defualt worker id
     */
    private static final Long DEFAULT_WORKER_ID = 1L;
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkWorkerNodeRepositoryImpl.class);

    @Override
    public synchronized final long assignWorkerId(String ipAddress, String appName) throws WorkerIdAssignException {
        try {
            WorkerNode workerNode = get(ipAddress, appName);
            if (workerNode == null) {
                //insert a new worker node
                workerNode = new WorkerNode();
                workerNode.setAppName(appName);
                workerNode.setCreated(new Date());
                workerNode.setModified(new Date());
                workerNode.setHostName(ipAddress);
                workerNode.setWorkerId(DEFAULT_WORKER_ID);
                insert(workerNode);
            }
            WorkerIdFileCache.writeCache(appName, workerNode);
            return workerNode.getWorkerId();
        } catch (Exception ex) {
            LOGGER.error("assign worker id error : " + ex.getMessage(), ex);
            LOGGER.warn("assign worker from local cache file");
            WorkerNode cachedWorkNode = (WorkerNode) WorkerIdFileCache.getDataFromFile(appName);
            if (cachedWorkNode != null) {
                return cachedWorkNode.getWorkerId();
            }
            throw new WorkerIdAssignException(ex.getMessage(), ex);
        }
    }


    /**
     * insert a worker node
     *
     * @param workerNode
     * @throws Exception
     */
    public abstract void insert(WorkerNode workerNode) throws Exception;

    /**
     * get a worker node
     *
     * @param hostName
     * @param appName
     * @return
     * @throws Exception
     */
    public abstract WorkerNode get(String hostName, String appName) throws Exception;
}
