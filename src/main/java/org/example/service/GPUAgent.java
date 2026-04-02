package org.example.service;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.GPU;
import org.example.domain.GPUMetric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GPUAgent {
    private final GPU gpu;
    private final StorageService storageService;
    private final ObjectMapper mapper;
    private final ScheduledExecutorService scheduler;
    private boolean isMonitoring;

    public StorageService getStorageService() { return storageService; }

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
            Runnable task = this::collectData;
            long delay = 60;
            scheduler.scheduleAtFixedRate(task, 0, delay, TimeUnit.SECONDS);
        } else{
            System.out.println("Monitoring is run");
        }
    }

    // сборщик данных
    private void collectData(){
        GPUMetric metric = gpu.computeMetrics();
        String keyStamp = metric.key();
        List<String> processes = gpu.getProcesses();
        List<String> activeUsers = mockUsers();
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("metrics", metric);
        jsonMap.put("processes", processes);
        jsonMap.put("users", activeUsers);
        try {
            String json = mapper.writeValueAsString(jsonMap);
            storageService.put(keyStamp, json);
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

    public void showData(){
        var map = storageService.getMap();

       Set<String> keys = map.keySet();
       for (String key : keys){
           try {
               var rawJson = mapper.readValue(map.get(key).toString(), Map.class);
               String json = mapper.writerWithDefaultPrettyPrinter()
                       .writeValueAsString(rawJson);
               System.out.println(json);
           } catch (JsonProcessingException e) {
               throw new RuntimeException(e);
           }
       }
    }

    public void close(){
        storageService.close();
    }

    private List<String> mockUsers(){
        return List.of("user1", "user2", "user3");
    }

    // проверяет активных пользователей
    private List<String> checkUsers(){
        ProcessBuilder pb = new ProcessBuilder("who");
        List<String> activeUsers = new ArrayList<>();
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while((line = reader.readLine()) != null){
                activeUsers.add(line);
            }
            process.destroy();
            return activeUsers;
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
