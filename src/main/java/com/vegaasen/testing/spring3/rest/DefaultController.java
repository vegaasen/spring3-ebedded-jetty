package com.vegaasen.testing.spring3.rest;

import com.vegaasen.testing.spring3.api.SomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
@RequestMapping(value = "/")
public class DefaultController {

    @Autowired
    private SomeService someService;

    @RequestMapping(
            value = "cool",
            method = {RequestMethod.GET},
            produces = {"application/xml"}
    )
    public
    @ResponseBody
    String getCoolStuff() {
        return someService.doStuffAndReturn();
    }

}
