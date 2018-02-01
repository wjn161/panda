/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.exception;

/**
 * @author wujn
 * @version $Id WorkerIdAssignException.java, v 0.1 2018-02-01 14:58 wujn Exp $$
 */
public class WorkerIdAssignException extends RuntimeException {


    private static final long serialVersionUID = 6184719548784605521L;

    /**
     * Default constructor
     */
    public WorkerIdAssignException() {
        super();
    }

    /**
     * Constructor with message & cause
     *
     * @param message
     * @param cause
     */
    public WorkerIdAssignException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message
     *
     * @param message
     */
    public WorkerIdAssignException(String message) {
        super(message);
    }

    /**
     * Constructor with message format
     *
     * @param msgFormat
     * @param args
     */
    public WorkerIdAssignException(String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
    }

    /**
     * Constructor with cause
     *
     * @param cause
     */
    public WorkerIdAssignException(Throwable cause) {
        super(cause);
    }
}
