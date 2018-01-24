/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen;

import me.wujn.panda.shardingjdbc.idgen.exception.IdGenerateException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author wujn
 * @version $Id BitsAllocator.java, v 0.1 2018-01-24 10:26 wujn Exp $$
 */
public class BitsAllocator {

    /**
     * total bits is 64
     */
    public static final short TOTAL_BITS = 1 << 6;
    private final short signBits = 1;
    private final short timestampBits;
    private final short workerIdBits;
    private final short sequenceBits;

    private final long maxWorkerId;
    private final long maxSequence;
    private final long maxDeltaSeconds;


    private final short timestampShift;
    private final short workerIdShift;

    public long allocate(long deltaSeconds, long workerId, long sequence) {
        return (deltaSeconds << timestampShift) | (workerId << workerIdShift) | sequence;
    }

    public BitsAllocator(short timestampBits, short workerIdBits, short sequenceBits) {
        // make sure allocated 64 bits
        short allocateTotalBits = (short) (signBits + timestampBits + workerIdBits + sequenceBits);
        if (allocateTotalBits != TOTAL_BITS) {
            throw new IdGenerateException("allocated bits not enough for 64 bits");
        }
        // initialize bits
        this.timestampBits = timestampBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;
        // initialize max value
        this.maxDeltaSeconds = ~(-1L << timestampBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);
        // initialize shift
        this.timestampShift = (short) (workerIdBits + sequenceBits);
        this.workerIdShift = sequenceBits;
    }

    /**
     * Getter method for property <tt>signBits</tt>.
     *
     * @return property value of signBits
     */
    public int getSignBits() {
        return signBits;
    }

    /**
     * Getter method for property <tt>timestampBits</tt>.
     *
     * @return property value of timestampBits
     */
    public short getTimestampBits() {
        return timestampBits;
    }

    /**
     * Getter method for property <tt>workerIdBits</tt>.
     *
     * @return property value of workerIdBits
     */
    public short getWorkerIdBits() {
        return workerIdBits;
    }

    /**
     * Getter method for property <tt>sequenceBits</tt>.
     *
     * @return property value of sequenceBits
     */
    public short getSequenceBits() {
        return sequenceBits;
    }

    /**
     * Getter method for property <tt>maxWorkerId</tt>.
     *
     * @return property value of maxWorkerId
     */
    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    /**
     * Getter method for property <tt>maxSequence</tt>.
     *
     * @return property value of maxSequence
     */
    public long getMaxSequence() {
        return maxSequence;
    }

    /**
     * Getter method for property <tt>maxDeltaSeconds</tt>.
     *
     * @return property value of maxDeltaSeconds
     */
    public long getMaxDeltaSeconds() {
        return maxDeltaSeconds;
    }

    /**
     * Getter method for property <tt>timestampShift</tt>.
     *
     * @return property value of timestampShift
     */
    public short getTimestampShift() {
        return timestampShift;
    }

    /**
     * Getter method for property <tt>workerIdShift</tt>.
     *
     * @return property value of workerIdShift
     */
    public short getWorkerIdShift() {
        return workerIdShift;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
