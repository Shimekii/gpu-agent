package org.example.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GPUMetric {
    private final Instant timestamp;
    private final int freeMemory;
    private final int temperature;

    public GPUMetric(Instant timestamp, int freeMemory, int temperature){
        this.timestamp = timestamp;
        this.freeMemory = freeMemory;
        this.temperature = temperature;
    }

    public String key(){
        LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return ldt.format(formatter);
    }

    @Override
    public String toString() {
        return "GPUMetric{" +
                "timestamp=" + timestamp +
                ", freeMemory=" + freeMemory +
                ", temperature=" + temperature +
                '}';
    }

    public Instant getTimestamp() { return timestamp; }
    public int getFreeMemory() { return freeMemory; }
    public int getTemperature() { return temperature; }
}

