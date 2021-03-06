/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.test;

import me.wujn.panda.shardingjdbc.idgen.IdGenerator;
import me.wujn.panda.shardingjdbc.idgen.worker.repos.WorkerNodeRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wujn
 * @version $Id IdGeneratorTest.java, v 0.1 2018-01-24 14:11 wujn Exp $$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring.xml")
public class IdGeneratorTest {

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private WorkerNodeRepository workerNodeRepository;
    private static final int SIZE = 1000000;

    @Test
    public void testNextId() throws InterruptedException {
        AtomicInteger control = new AtomicInteger(-1);
        Set<String> uidSet = new ConcurrentSkipListSet<>();
        // Initialize threads
        List<Thread> threadList = new ArrayList<>(8);
        for (int i = 0; i < 64; i++) {
            Thread thread = new Thread(() -> workerRun(uidSet, control));
            thread.setName("UID-generator-" + i);
            threadList.add(thread);
            thread.start();
        }
        // Wait for worker done
        for (Thread thread : threadList) {
            thread.join();
        }
        // Check generate 10w times
        Assert.assertEquals(SIZE, control.get());
        // Check UIDs are all unique
        checkUniqueID(uidSet);
    }

    private void workerRun(Set<String> uidSet, AtomicInteger control) {
        for (; ; ) {
            int myPosition = control.updateAndGet(old -> (old == SIZE ? SIZE : old + 1));
            if (myPosition == SIZE) {
                return;
            }
            doGenerate(uidSet, myPosition);
        }
    }

    private void doGenerate(Set<String> uidSet, int index) {
        String uid = idGenerator.getStrId("00000");
        System.out.println(uid);
        uidSet.add(uid);
    }

    private void checkUniqueID(Set<String> uidSet) {
        System.out.println(uidSet.size());
        Assert.assertEquals(SIZE, uidSet.size());
    }
}
