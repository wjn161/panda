/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker;

/**
 * @author wujn
 * @version $Id WorkerNodeRepository.java, v 0.1 2018-01-24 16:58 wujn Exp $$
 */
public interface WorkerNodeRepository {
    /**
     * update worker node
     *
     * @param workerNode
     */
    void update(WorkerNode workerNode);

    /**
     * insert worker node
     *
     * @param workerNode
     */
    void insert(WorkerNode workerNode);

    /**
     * query worker node
     *
     * @param hostName
     * @param port
     * @return
     */
    WorkerNode get(String hostName, String port);

}
