/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.repository;

/**
 * @author wujn
 * @version $Id WorkerNodeRepository.java, v 0.1 2018-01-24 16:58 wujn Exp $$
 */
public interface WorkerNodeRepository {
    /**
     * insert worker node
     *
     * @param workerNode
     */
    void insert(WorkerNode workerNode) throws Exception;

    /**
     * query worker node
     *
     * @param hostName
     * @param appName
     * @return
     */
    WorkerNode get(String hostName, String appName) throws Exception;

}
