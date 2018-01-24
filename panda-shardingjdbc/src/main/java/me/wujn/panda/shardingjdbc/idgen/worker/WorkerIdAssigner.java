/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker;

/**
 * @author wujn
 * @version $Id WorkerIdAssigner.java, v 0.1 2018-01-24 10:29 wujn Exp $$
 */
public interface WorkerIdAssigner {
    /**
     * assign a worker id
     *
     * @return
     */
    long assignWorkerId();

    /**
     * reset workerId
     */
    void resetWorkerId();
}
