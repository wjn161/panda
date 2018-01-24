/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wujn
 * @version $Id ReuseableWorkerIdAssigner.java, v 0.1 2018-01-24 10:30 wujn Exp $$
 */
@Service
public class ReuseableWorkerIdAssigner implements WorkerIdAssigner {

    @Autowired
    private WorkerNodeRepository workerNodeRepository;

    @Override
    public long assignWorkerId() {
        return 0;
    }

    @Override
    public void resetWorkerId() {

    }
}
