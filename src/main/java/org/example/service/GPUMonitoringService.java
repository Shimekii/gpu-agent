package org.example.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.grpc.Empty;
import org.example.grpc.GPUMetric;
import org.example.grpc.GPUMonitoringGrpc;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GPUMonitoringService extends GPUMonitoringGrpc.GPUMonitoringImplBase {

    private static final Logger logger = Logger.getLogger(GPUMonitoringService.class.getName());
    private final StorageService storageService;

    public GPUMonitoringService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public void getLatestMetric(Empty request, StreamObserver<GPUMetric> responseObserver) {
        try {
            Map<String, Object> map = storageService.getMap();
            if (map.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("No available metrics").asException());
            }

            TreeSet<String> keys = new TreeSet<>(map.keySet());
            String lastKey = keys.last();
            String json = map.get(lastKey).toString();
            GPUMetric metric = convertJsonToProto(json);
            responseObserver.onNext(metric);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getLatestMetric", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal error").asException());
        }
    }

    @Override
    public void getAllMetrics(Empty request, StreamObserver<GPUMetric> responseObserver) {
        try {
            Map<String, Object> map = storageService.getMap();
            int sentCount = 0;

            for (String key : map.keySet()) {
                try{
                    String json = map.get(key).toString();
                    GPUMetric metric = convertJsonToProto(json);
                    responseObserver.onNext(metric);
                    sentCount++;
                } catch (Exception e) {
                    logger.warning("Failed to parse key: " + key + ": " + e.getMessage());
                }
            }
            logger.info("Sent " + sentCount + " metrics");
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getAllMetrics", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal error").asException());
        }
    }

    private GPUMetric convertJsonToProto(String json) throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Map<String, Object> jsonMap = mapper.readValue(json, Map.class);
        Map<String, Object> metricsMap = (Map<String, Object>) jsonMap.get("metrics");
        List<String> processes = (List<String>) jsonMap.get("processes");
        int freeMemory = (int) metricsMap.get("freeMemory");
        int temperature = (int) metricsMap.get("temperature");
        String timestamp = metricsMap.get("timestamp").toString();

        GPUMetric.Builder builder = GPUMetric.newBuilder()
                .setFreeMemoryMb(freeMemory)
                .setTemperatureC(temperature)
                .setTimestamp(timestamp);
        if (processes != null) {
            builder.addAllProcesses(processes);
        }

        return builder.build();
    }
}
