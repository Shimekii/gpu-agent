package org.example.service;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.GPU;
import org.example.domain.GPUMetric;
import org.example.domain.GPUProcess;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GPUAgent {
    private final GPU gpu;
    private final StorageService storageService;
    private final ObjectMapper mapper;
    private final ScheduledExecutorService scheduler;
    private boolean isMonitoring;

    public GPUAgent(){
        gpu = new GPU();
        storageService = new StorageService("storage/data.mv.db", "logMap");
        mapper = new ObjectMapper();
        // Сериализуем любые поля
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        // Подключаем модуль для Instant
        mapper.registerModule(new JavaTimeModule());
        // Отключаем запись дат в формате timestamp
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Планировщик задач с одним рабочим потоком
        scheduler = Executors.newScheduledThreadPool(1);
        isMonitoring = false;
    }

    public void startMonitoring(){
        if (!isMonitoring) {
            System.out.println("Start monitoring...");
            isMonitoring = true;
            Runnable task = () -> collectData();
            long delay = 60;
            scheduler.scheduleAtFixedRate(task, 0, delay, TimeUnit.SECONDS);
        } else{
            System.out.println("Monitoring is run");
        }
    }

    // сборщик данных
    private void collectData(){
        GPUMetric metric = gpu.computeMetrics();
        List<GPUProcess> processes = gpu.getProcesses();

        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("metrics", metric);
        jsonMap.put("processes", processes);
        try {
            String json = mapper.writeValueAsString(jsonMap);
            storageService.put("owner", json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopMonitoring(){
        if (isMonitoring) {
            System.out.println("Stop monitoring...");
            scheduler.shutdown();
        } else {
            System.out.println("Monitoring not run");
        }
    }

    public void showData(String key){
        var json = storageService.get(key);
        try {
            var obj = mapper.readValue(json.toString(), Map.class);
            String pretty = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
            System.out.println(pretty);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        storageService.close();
    }
}
