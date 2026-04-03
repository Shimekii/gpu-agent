package org.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.Request;
import org.example.service.StorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestRepository {
    private final Map<String, Object> db;
    private final ObjectMapper mapper;

    public RequestRepository(StorageService storageService) {
        this.db = storageService.getStore().openMap("requestMap");
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public void save(Request request) throws Exception {
        db.put(request.getUserLogin() + "_" + System.currentTimeMillis(), mapper.writeValueAsString(request));
    }

    public List<Request> findAll() throws Exception {
        List<Request> requests = new ArrayList<>();
        for (Object value : db.values()) {
            requests.add(mapper.readValue(value.toString(), Request.class));
        }
        return requests;
    }
}
