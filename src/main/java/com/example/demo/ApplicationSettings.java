package com.example.demo;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("demo-converter")
@Component
public class ApplicationSettings {

    private List<String> baseMarks;

    public List<String> getBaseMarks() {
        return baseMarks;
    }

    public void setBaseMarks(List<String> baseMarks) {
        this.baseMarks = baseMarks;
    }
}
