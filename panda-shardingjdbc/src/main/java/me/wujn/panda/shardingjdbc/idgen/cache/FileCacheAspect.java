/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.cache;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author wujn
 * @version $Id FileCacheAspect.java, v 0.1 2018-01-29 11:50 wujn Exp $$
 */
@Aspect
@Component
public class FileCacheAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileCacheAspect.class);

    @Pointcut("@annotation(me.wujn.panda.shardingjdbc.idgen.cache.FileCache)")
    public void fileCacheAspect() {

    }

    @Before("fileCacheAspect()")
    public void beforeInvoke(JoinPoint joinPoint) {
        if (joinPoint != null) {
        }
        LOGGER.info(joinPoint.toLongString());
    }

    @After("fileCacheAspect()")
    public void afterInvoke(JoinPoint joinPoint) {
        LOGGER.info(joinPoint.toLongString());
    }
}
