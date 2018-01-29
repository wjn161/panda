/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.repository;

import me.wujn.panda.shardingjdbc.idgen.cache.FileCache;
import me.wujn.panda.shardingjdbc.idgen.utils.JavaSerializer;
import me.wujn.panda.shardingjdbc.idgen.utils.NetUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.wujn.panda.shardingjdbc.idgen.utils.Constants.CACHE_FILE_PATH;

/**
 * @author wujn
 * @version $Id ZkWorkerNodeRepositoryImpl.java, v 0.1 2018-01-24 16:58 wujn Exp $$
 */
@Service
public class ZkWorkerNodeRepositoryImpl implements WorkerNodeRepository {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkWorkerNodeRepositoryImpl.class);
    /**
     * zookeeper base path
     */
    private static final String ZK_PATH = "/idgen/workerid";
    /**
     * zookeeper lock path
     */
    private static final String ZK_LOCK_PATH = ZK_PATH + "/locker";
    /**
     * zookeeper connect retry times
     */
    private static final int ZK_RETRY_TIME = 5;
    /**
     * zookeeper address
     */
    private String zookeeperAddress;

    @SuppressWarnings("unchecked")
    @Override
    public void insert(WorkerNode workerNode) throws Exception {
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(
                    zookeeperAddress,
                    30000,
                    5000,
                    new RetryNTimes(ZK_RETRY_TIME, 1000)
            );
            if (zkClient.getState() != CuratorFrameworkState.STARTED) {
                zkClient.start();
            }
            LOGGER.info("zookeeper server {} connected,client: {}", zookeeperAddress, NetUtils.getLocalAddress());
            String zkPath = MessageFormat.format("{0}/{1}", ZK_PATH, workerNode.getAppName());
            InterProcessMutex lock = new InterProcessMutex(zkClient, ZK_LOCK_PATH);
            // acquire a lock 3 seconds for timeout
            if (lock.acquire(3, TimeUnit.SECONDS)) {
                try {
                    //check path exist
                    Stat stat = zkClient.checkExists().forPath(zkPath);
                    //create a new node if not exist
                    if (stat == null) {
                        List<WorkerNode> list = new ArrayList<>();
                        list.add(workerNode);
                        zkClient.create()
                                .creatingParentsIfNeeded()
                                .withMode(CreateMode.PERSISTENT)
                                .forPath(zkPath, JavaSerializer.serialize(list));
                    } else {
                        //or else add the data to the list
                        byte[] bytesData = zkClient.getData().forPath(zkPath);
                        if (bytesData != null) {
                            List<WorkerNode> sourceList = (List<WorkerNode>) JavaSerializer.deserialize(bytesData);
                            if (sourceList != null) {
                                boolean exist = sourceList.stream().anyMatch(item -> item.getHostName().equals(workerNode.getHostName()));
                                //if node has already in list, do nothing!
                                if (exist) {
                                    return;
                                }
                                WorkerNode max = sourceList.stream().max((o1, o2) -> (int) (o1.getWorkerId() - o2.getWorkerId())).orElse(null);
                                if (max != null) {
                                    long current = max.getWorkerId() + 1;
                                    workerNode.setWorkerId(current);
                                    sourceList.add(workerNode);
                                    zkClient.setData().forPath(zkPath, JavaSerializer.serialize(sourceList));
                                }
                            }
                        }
                    }
                } finally {
                    //release lock
                    lock.release();
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }

    @FileCache
    @SuppressWarnings("unchecked")
    @Override
    public WorkerNode get(String hostName, String appName) throws Exception {
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(
                    zookeeperAddress,
                    30000,
                    5000,
                    new RetryNTimes(ZK_RETRY_TIME, 1000)
            );
            if (zkClient.getState() != CuratorFrameworkState.STARTED) {
                zkClient.start();
            }
            LOGGER.info("zookeeper server {} connected,client: {}", zookeeperAddress, NetUtils.getLocalAddress());
            //检查路径是否存在
            String zkPath = MessageFormat.format("{0}/{1}", ZK_PATH, appName);
            Stat stat = zkClient.checkExists().forPath(zkPath);
            if (stat != null) {
                //获取数据
                byte[] bytesData = zkClient.getData().forPath(zkPath);
                if (bytesData != null) {
                    List<WorkerNode> sourceList = (List<WorkerNode>) JavaSerializer.deserialize(bytesData);
                    if (sourceList != null) {
                        WorkerNode node = sourceList.stream().filter(item -> item.getHostName().equals(hostName)).findFirst().orElse(null);
                        if (node == null) {
                            return null;
                        }
                        return node;
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
        return null;
    }

    /**
     * Setter method for property <tt>zookeeperAddress</tt>.
     *
     * @param zookeeperAddress value to be assigned to property zookeeperAddress
     */
    public void setZookeeperAddress(String zookeeperAddress) {
        this.zookeeperAddress = zookeeperAddress;
    }
}
