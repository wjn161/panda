/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.cache;

import java.lang.annotation.*;

/**
 * @author wujn
 * @version $Id FileCache.java, v 0.1 2018-01-29 11:38 wujn Exp $$
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FileCache {

    String path() default ".idgen/workerid/default-app/";
}
