/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.cache;

import me.wujn.panda.shardingjdbc.idgen.utils.JavaSerializer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * worker id
 *
 * @author wujn
 * @version $Id WorkerIdFileCache.java, v 0.1 2018-01-29 11:21 wujn Exp $$
 */
public class WorkerIdFileCache {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerIdFileCache.class);

    /**
     * get default file cache path
     * eg:
     * linux:/home/someuser/.idgen/workerid/default-app
     * windows : C:\Users\someuser\.idgen\workerid\default-app
     *
     * @return
     */
    private static String getCacheFileName(String appName) {
        return System.getProperty("user.home") + File.separator + ".idgen" + File.separator + "workerid" + File.separator + appName + ".cache";
    }

    public static void writeCache(String appName, Object data) {
        try {
            File file = new File(getCacheFileName(appName));
            LOGGER.info("create worker id cache file in {}", file);
            FileUtils.writeByteArrayToFile(file, JavaSerializer.serialize(data));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    public static Object getDataFromFile(String appName) {
        try {
            File file = new File(getCacheFileName(appName));
            byte[] data = FileUtils.readFileToByteArray(file);
            if (data != null) {
                return JavaSerializer.deserialize(data);
            }
            return null;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
}
