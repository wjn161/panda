/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen;

import me.wujn.panda.shardingjdbc.idgen.exception.IdGenerateException;

/**
 * @author wujn
 * @version $Id IdGenerator.java, v 0.1 2018-01-19 13:47 wujn Exp $$
 */
public interface IdGenerator {
    /**
     * Get a unique ID
     *
     * @return ID
     * @throws IdGenerateException
     */
    Long getId() throws IdGenerateException;

    /**
     * Parse the UID into elements which are used to generate the UID. <br>
     * Such as timestamp & workerId & sequence...
     *
     * @param uid
     * @return Parsed info
     */
    String parseId(long uid);
}
