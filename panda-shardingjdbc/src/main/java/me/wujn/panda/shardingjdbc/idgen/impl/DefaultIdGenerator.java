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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents an implementation of {@link IdGenerator}
 * The unique id has 64bits (long), default allocated as blow:<br>
 * <ul>
 * <li> 1. sign: The highest bit is 0 </li>
 * <li> 2. delta seconds: The next 40 bits represents delta seconds since a customer epoch(2018-01-01 00:00:00.000).supports about 34 years
 * until to 2052-01-01 00:00:00</li>
 * <li> 3. worker node id: The next 12 bits represents 4096 worker nodes per business unit</li>
 * <li> 4. sequence: The next 11 bits represents a sequence within the same second, max for 4096/ms </li>
 * </ul>
 * <pre>{@code
 * +------+---------------+--------+--------------
 * | sign | delta seconds | worker id | sequence |
 * +------+---------------+--------+--------------
 * | 1bit |     40bits    |   12bits  |  11bits  |
 * +------+----------------------+----------------
 * }</pre>
 * snowflake details:https://segmentfault.com/a/1190000011282426
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
    protected short workerBits = 12;
    protected short seqBits = 11;

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
     * string id settings
     */
    protected short sequenceLength = 4;
    protected short workerIdLength = 4;
    protected short customerDataLength = 5;
    /**
     * Spring property
     */
    protected WorkerIdAssigner workerIdAssigner;

    @Override
    public Long getId() throws IdGenerateException {
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
    public String getStrId(String exData) throws IdGenerateException {
        try {
            return nextStrId(exData);
        } catch (Exception e) {
            LOGGER.error("Generate unique id exception. ", e);
            throw new IdGenerateException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        bitsAllocator = new BitsAllocator(timeBits, workerBits, seqBits);
        workerId = workerIdAssigner.assignWorkerId();
        LOGGER.info("worker id is {}", workerId);
        if (workerId > bitsAllocator.getMaxWorkerId()) {
            throw new IdGenerateException(MessageFormat.format("worker id is illegal,workerid={0},max workerId={1}",
                    workerId, bitsAllocator.getMaxWorkerId()));
        }
        LOGGER.info("init bits(1,{},{},{})", timeBits, workerBits, seqBits);
    }

    private synchronized Long nextId() {
        long currentMillSeconds = getCurrentMillSeconds();
        // Clock moved backwards, refuse to generate uid
        if (currentMillSeconds < lastSecond) {
            long refusedSeconds = lastSecond - currentMillSeconds;
            throw new IdGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
        }
        // At the same second, increase sequence
        if (currentMillSeconds == lastSecond) {
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // Exceed the max sequence, we wait the next second to generate uid
            if (sequence == 0) {
                currentMillSeconds = getNextMillSeconds(lastSecond);
            }
            // At the different millsecond, sequence restart from a random number between 0 from 9
        } else {
            sequence = RandomUtils.nextLong(0, 9);
        }
        lastSecond = currentMillSeconds;
        // Allocate bits for ID
        return bitsAllocator.allocate(currentMillSeconds - epochSeconds, workerId, sequence);
    }

    private synchronized String nextStrId(String exData) {
        //check exData length
        if (StringUtils.isBlank(exData) || exData.length() != customerDataLength) {
            throw new IllegalArgumentException("exData's length must be 4");
        }
        long currentMillSeconds = getCurrentMillSeconds();
        // Clock moved backwards, refuse to generate uid
        if (currentMillSeconds < lastSecond) {
            long refusedSeconds = lastSecond - currentMillSeconds;
            throw new IdGenerateException("Clock moved backwards. Refusing for %d seconds", refusedSeconds);
        }
        // At the same second, increase sequence
        if (currentMillSeconds == lastSecond) {
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // Exceed the max sequence, we wait the next second to generate uid
            if (sequence == 0) {
                currentMillSeconds = getNextMillSeconds(lastSecond);
            }
            // At the different millsecond, sequence restart from a random number betwenn 0 from 9
        } else {
            sequence = RandomUtils.nextLong(0, 9);
        }
        lastSecond = currentMillSeconds;
        // Allocate bits for ID
        return DateFormatUtils.format(currentMillSeconds, "yyyyMMddHHmmssSSS")
                + exData
                + StringUtils.leftPad(String.valueOf(workerId), workerIdLength, '0')
                + StringUtils.leftPad(String.valueOf(sequence), sequenceLength, '0');
    }

    private long getCurrentMillSeconds() {
        long currentSecond = SystemClock.millisClock().now();
        if (currentSecond - epochSeconds > bitsAllocator.getMaxDeltaSeconds()) {
            throw new IdGenerateException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }
        return currentSecond;
    }

    private long getNextMillSeconds(long lastTimestamp) {
        long timestamp = getCurrentMillSeconds();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentMillSeconds();
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

    /**
     * Setter method for property <tt>sequenceLength</tt>.
     *
     * @param sequenceLength value to be assigned to property sequenceLength
     */
    public void setSequenceLength(short sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    /**
     * Setter method for property <tt>workerIdLength</tt>.
     *
     * @param workerIdLength value to be assigned to property workerIdLength
     */
    public void setWorkerIdLength(short workerIdLength) {
        this.workerIdLength = workerIdLength;
    }

    /**
     * Setter method for property <tt>customerDataLength</tt>.
     *
     * @param customerDataLength value to be assigned to property customerDataLength
     */
    public void setCustomerDataLength(short customerDataLength) {
        this.customerDataLength = customerDataLength;
    }
}
