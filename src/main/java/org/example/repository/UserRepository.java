package org.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.domain.User;
import org.example.service.StorageService;
import java.util.Map;

public class UserRepository {
    private final Map<String, Object> db;
    private final ObjectMapper mapper;
    private final StorageService storageService;

    public UserRepository(StorageService storageService) {
        this.storageService = storageService;
        // отдельный map для пользователей
        this.db = storageService.getStore().openMap("userMap");

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(User user) throws Exception {
        db.put(user.getLogin(), mapper.writeValueAsString(user));
    }

    public User findByLogin(String login) throws Exception {
        Object data = db.get(login);
        if (data == null) return null;
        return mapper.readValue(data.toString(), User.class);
    }

    public StorageService getStorageService() {
        return storageService;
    }
}
