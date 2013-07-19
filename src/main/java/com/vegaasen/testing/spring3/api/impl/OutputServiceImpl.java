package com.vegaasen.testing.spring3.api.impl;

import com.vegaasen.testing.spring3.api.OutputService;

/**
 * Created with IntelliJ IDEA.
 * User: WindowsUser
 * Date: 7/19/13
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class OutputServiceImpl implements OutputService {

    @Override
    public void outputMessage(String msg) {
        if (msg == null || msg.isEmpty()) {
            throw new AssertionError();
        }
        System.out.println(msg);
    }

}
