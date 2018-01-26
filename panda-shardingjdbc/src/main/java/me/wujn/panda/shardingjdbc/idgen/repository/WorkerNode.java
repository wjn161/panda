/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.repository;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wujn
 * @version $Id WorkerNode.java, v 0.1 2018-01-24 17:00 wujn Exp $$
 */
public class WorkerNode implements Serializable {
    private static final long serialVersionUID = 8838477449259913091L;
    private Long workerId;
    private String hostName;
    private String appName;
    private Date modified;
    private Date created;

    /**
     * Getter method for property <tt>workerId</tt>.
     *
     * @return property value of workerId
     */
    public Long getWorkerId() {
        return workerId;
    }

    /**
     * Setter method for property <tt>workerId</tt>.
     *
     * @param workerId value to be assigned to property workerId
     */
    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    /**
     * Getter method for property <tt>hostName</tt>.
     *
     * @return property value of hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Setter method for property <tt>hostName</tt>.
     *
     * @param hostName value to be assigned to property hostName
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Getter method for property <tt>appName</tt>.
     *
     * @return property value of appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Setter method for property <tt>appName</tt>.
     *
     * @param appName value to be assigned to property appName
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Getter method for property <tt>modified</tt>.
     *
     * @return property value of modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Setter method for property <tt>modified</tt>.
     *
     * @param modified value to be assigned to property modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Getter method for property <tt>created</tt>.
     *
     * @return property value of created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Setter method for property <tt>created</tt>.
     *
     * @param created value to be assigned to property created
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
