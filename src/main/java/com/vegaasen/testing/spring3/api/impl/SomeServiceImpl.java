package com.vegaasen.testing.spring3.api.impl;

import com.vegaasen.testing.spring3.api.OutputService;
import com.vegaasen.testing.spring3.api.SomeService;
import org.springframework.beans.factory.annotation.Autowired;

public class SomeServiceImpl implements SomeService {

    private int maxIterations;
    private String textToOutput;

    @Autowired
    private OutputService outputService;

    @Override
    public void doStuff() {
        for (int i = 0; i < maxIterations; i++) {
            outputService.outputMessage(textToOutput);
        }
    }

    @Override
    public String doStuffAndReturn() {
        String text = "";
        for (int i = 0; i < maxIterations; i++) {
            text += textToOutput;
        }
        return text;
    }

    @Override
    public String doStuffWithInput(String input) {
        for (int i = 0; i < maxIterations; i++) {
            outputService.outputMessage(input);
        }
        return "";
    }

    public void setTextToOutput(String textToOutput) {
        this.textToOutput = textToOutput;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }
}
