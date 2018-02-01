/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker.repos;

import me.wujn.panda.shardingjdbc.idgen.exception.WorkerIdAssignException;

/**
 * @author wujn
 * @version $Id WorkerNodeRepository.java, v 0.1 2018-01-24 16:58 wujn Exp $$
 */
public interface WorkerNodeRepository {


    /**
     * assign a worker id
     *
     * @param ipAddress
     * @param appName
     * @return
     * @throws WorkerIdAssignException
     */
    long assignWorkerId(String ipAddress, String appName) throws WorkerIdAssignException;
}
