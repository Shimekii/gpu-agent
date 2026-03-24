package org.example.domain;

import java.time.Instant;
import java.util.Date;

public class GPUMetric {
    private final Instant timestamp;
    private final int freeMemory;
    private final int temperature;

    public GPUMetric(Instant timestamp, int freeMemory, int temperature){
        this.timestamp = timestamp;
        this.freeMemory = freeMemory;
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "GPUMetric{" +
                "timestamp=" + timestamp +
                ", freeMemory=" + freeMemory +
                ", temperature=" + temperature +
                '}';
    }
}

