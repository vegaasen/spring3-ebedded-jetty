package com.vegaasen.testing.spring3.loader;

import com.vegaasen.testing.spring3.api.SomeService;
import com.vegaasen.testing.spring3.runner.SpringJUnit4ClassRunnerWithSystemProps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunnerWithSystemProps.class)
@ContextConfiguration(locations = {"classpath:applicationContext-test.xml.old"})
public class BeanLoader {

    @Autowired
    @Qualifier("someService")
    private SomeService someService;

    @Before
    public void setUp() {
        assertNotNull(someService);
    }

    @Test
    public void shouldLoadSomeMessages() {
        String wtf = someService.doStuffAndReturn();
        assertNotNull(wtf);
        assertFalse(wtf.isEmpty());
        assertTrue(wtf.length() > 1);
    }

}
