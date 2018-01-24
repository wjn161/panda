/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.impl;

import me.wujn.panda.shardingjdbc.idgen.BitsAllocator;
import me.wujn.panda.shardingjdbc.idgen.IdGenerator;
import me.wujn.panda.shardingjdbc.idgen.exception.IdGenerateException;
import me.wujn.panda.shardingjdbc.idgen.utils.DateUtils;
import me.wujn.panda.shardingjdbc.idgen.utils.SystemClock;
import me.wujn.panda.shardingjdbc.idgen.worker.ReuseableWorkerIdAssigner;
import me.wujn.panda.shardingjdbc.idgen.worker.WorkerIdAssigner;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents an implementation of {@link IdGenerator}
 * The unique id has 64bits (long), default allocated as blow:<br>
 * <ul>
 * <li> 1. sign: The highest bit is 0 </li>
 * <li> 2. delta seconds: The next 40 bits represents delta seconds since a customer epoch(2018-01-01 00:00:00.000).supports about 34 years
 * until to 2052-01-01 00:00:00</li>
 * <li> 3. worker node id: The next 10 bits represents 1024 worker nodes per business unit</li>
 * <li> 4. sequence: The next 13 bits represents a sequence within the same second, max for 8192/ms </li>
 * </ul>
 * <pre>{@code
 * +------+---------------+--------+--------------
 * | sign | delta seconds | worker id | sequence |
 * +------+---------------+--------+--------------
 * | 1bit |     40bits    |   10bits  |  13bits  |
 * +------+----------------------+----------------
 * }</pre>
 *
 * @author wujn
 * @version $Id DefaultIdGenerator.java, v 0.1 2018-01-19 13:49 wujn Exp $$
 */
public class DefaultIdGenerator implements IdGenerator, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReuseableWorkerIdAssigner.class);
    /**
     * Bits allocate
     */
    protected short timeBits = 40;
    protected short workerBits = 10;
    protected short seqBits = 13;

    /**
     * Customer epoch, unit as second. For example 2018-01-01 (ms: 1514736000000L)
     */
    protected String epochStr = "2018-01-01";
    protected long epochSeconds = 1514736000000L;

    /**
     * Stable fields after spring bean initializing
     */
    protected BitsAllocator bitsAllocator;
    protected long workerId;

    /**
     * Volatile fields caused by nextId()
     */
    protected long sequence = 0L;
    protected long lastSecond = -1L;

    /**
     * Spring property
     */
    protected WorkerIdAssigner workerIdAssigner;

    @Override
    public long getId() throws IdGenerateException {
        try {
            return nextId();
        } catch (Exception e) {
            LOGGER.error("Generate unique id exception. ", e);
            throw new IdGenerateException(e);
        }
    }

    @Override
    public String parseId(long id) {
        long totalBits = BitsAllocator.TOTAL_BITS;
        long signBits = bitsAllocator.getSignBits();
        long timestampBits = bitsAllocator.getTimestampBits();
        long workerIdBits = bitsAllocator.getWorkerIdBits();
        long sequenceBits = bitsAllocator.getSequenceBits();
        // parse UID
        long sequence = (id << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);
        long workerId = (id << (timestampBits + signBits)) >>> (totalBits - workerIdBits);

        long deltaSeconds = id >>> (workerIdBits + sequenceBits);
        Date thatTime = new Date(epochSeconds + deltaSeconds);
        String thatTimeStr = DateUtils.format(thatTime, DateUtils.SIMPLE_DATETIME_FORMAT);
        // format as string
        return String.format("{\"ID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
                id, thatTimeStr, workerId, sequence);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        bitsAllocator = new BitsAllocator(timeBits, workerBits, seqBits);
        workerId = workerIdAssigner.assignWorkerId();
        if (workerId > bitsAllocator.getMaxWorkerId()) {
            workerIdAssigner.resetWorkerId();
            workerId = workerIdAssigner.assignWorkerId();
        }
        LOGGER.info("init bits(1,{},{},{})", timeBits, workerBits, seqBits);
    }

    private synchronized long nextId() {
        long currentSecond = getCurrentSecond();
        // Clock moved backwards, refuse to generate uid
        if (currentSecond < lastSecond) {
            long refusedSeconds = lastSecond - currentSecond;
            throw new IdGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
        }

        // At the same second, increase sequence
        if (currentSecond == lastSecond) {
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // Exceed the max sequence, we wait the next second to generate uid
            if (sequence == 0) {
                currentSecond = getNextSecond(lastSecond);
            }
            // At the different second, sequence restart from a random number betwenn 0 from 9
        } else {
            sequence = RandomUtils.nextLong(0, 9);
        }
        lastSecond = currentSecond;
        // Allocate bits for ID
        return bitsAllocator.allocate(currentSecond - epochSeconds, workerId, sequence);
    }

    private long getCurrentSecond() {
        long currentSecond = SystemClock.millisClock().now();
        if (currentSecond - epochSeconds > bitsAllocator.getMaxDeltaSeconds()) {
            throw new IdGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }
        return currentSecond;
    }

    private long getNextSecond(long lastTimestamp) {
        long timestamp = getCurrentSecond();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentSecond();
        }
        return timestamp;
    }

    public void setWorkerIdAssigner(WorkerIdAssigner workerIdAssigner) {
        this.workerIdAssigner = workerIdAssigner;
    }

    /**
     * Setter method for property <tt>timeBits</tt>.
     *
     * @param timeBits value to be assigned to property timeBits
     */
    public void setTimeBits(short timeBits) {
        this.timeBits = timeBits;
    }

    /**
     * Setter method for property <tt>workerBits</tt>.
     *
     * @param workerBits value to be assigned to property workerBits
     */
    public void setWorkerBits(short workerBits) {
        this.workerBits = workerBits;
    }

    /**
     * Setter method for property <tt>seqBits</tt>.
     *
     * @param seqBits value to be assigned to property seqBits
     */
    public void setSeqBits(short seqBits) {
        this.seqBits = seqBits;
    }

    /**
     * Setter method for property <tt>epochStr</tt>.
     *
     * @param epochStr value to be assigned to property epochStr
     */
    public void setEpochStr(String epochStr) {
        if (StringUtils.isNotBlank(epochStr)) {
            Date epochDate;
            this.epochStr = epochStr;
            try {
                epochDate = DateUtils.parseDate(epochStr, DateUtils.SIMPLE_DATE_FORMAT);
            } catch (Exception ex) {
                epochDate = new Date();
                LOGGER.error("epochstr date format error it must be yyyy-MM-dd", ex);
            }
            this.epochSeconds = TimeUnit.MILLISECONDS.toSeconds(epochDate.getTime());
        }
    }

    public static void main(String[] args) {
        DefaultIdGenerator defaultIdGenerator = new DefaultIdGenerator();
        System.out.println(defaultIdGenerator.nextId());
    }
}
