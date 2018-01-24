/**
 * Zentech-Inc
 * Copyright (C) 2018 All Rights Reserved.
 */
package me.wujn.panda.shardingjdbc.idgen.test;

import me.wujn.panda.shardingjdbc.idgen.IdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author wujn
 * @version $Id IdGeneratorTest.java, v 0.1 2018-01-24 14:11 wujn Exp $$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class IdGeneratorTest {

    @Autowired
    private IdGenerator idGenerator;

    @Test
    public void testNextId() {
        long id = idGenerator.getId();
        long id2 = idGenerator.getId();
        System.out.println(id);
        System.out.println(idGenerator.parseId(id));
        System.out.println(idGenerator.parseId(id2));
    }

}
