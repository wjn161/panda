/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.test;

import junit.framework.TestCase;
import me.wujn.panda.shardingjdbc.idgen.IdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author wujn
 * @version $Id IdGeneratorConcurrentTest.java, v 0.1 2018-01-29 11:04 wujn Exp $$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class IdGeneratorConcurrentTest extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdGeneratorConcurrentTest.class);

    @Autowired
    private IdGenerator idGenerator;

    private static final int SIZE = 1000000;

    private static void assertConcurrent(final String message,
                                         final List<? extends Runnable> runnables,
                                         final int maxTimeoutSeconds, final int maxThreadPoolSize) throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(message);

        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections
                .synchronizedList(new ArrayList<>());
        final ExecutorService threadPool = Executors
                .newFixedThreadPool(numThreads > maxThreadPoolSize ? maxThreadPoolSize : numThreads);
        try {
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(() -> {
                    try {
                        afterInitBlocker.await();   // 相当于加了个阀门等待所有任务都准备就绪
                        submittedTestRunnable.run();
                    } catch (final Throwable e) {
                        exceptions.add(e);
                    } finally {
                        allDone.countDown();
                    }
                });
            }
            // start all test runners
            afterInitBlocker.countDown();
            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue(message + "failed with exception(s)" + exceptions,
                exceptions.isEmpty());
    }

    @Test
    public void testAssertConcurrent() throws InterruptedException {
        List<Runnable> tasks = new ArrayList<>(100);
        for (int i = 0; i < 1; i++) {
            tasks.add(() -> {
                for (int x = 0; x < SIZE; x++) {
                    idGenerator.getId();
                }
            });
        }
        assertConcurrent("10000tasks", tasks, 100, 1000);
    }
}
