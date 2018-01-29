/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.cache;

import me.wujn.panda.shardingjdbc.idgen.repository.WorkerNode;

import java.io.File;

/**
 * worker id
 *
 * @author wujn
 * @version $Id WorkerIdFileCache.java, v 0.1 2018-01-29 11:21 wujn Exp $$
 */
public class WorkerIdFileCache {
    /**
     * get default file cache path
     * eg:
     * linux:/root/.idgen/workerid/default-app
     * windows : C:\Users\someuser\.idgen\workerid\default-app
     *
     * @return
     */
    public static String getDefaultFileCachePath(String appName) {
        return System.getProperty("user.home") + File.separator + ".idgen" + File.separator + "workerid" + File.separator + appName;
    }

    public static void put(WorkerNode node) {

    }

    public static WorkerNode get(String ipAddress, String appName) {
        return null;
    }
}
