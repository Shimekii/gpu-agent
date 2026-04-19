package org.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.domain.User;
import org.example.service.StorageService;

import java.util.ArrayList;
import java.util.List;
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

    public User findByLogin(String login) {
        try {
            Object data = db.get(login);
            if (data == null) return null;
            return mapper.readValue(data.toString(), User.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Метод для получения всех пользователей
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (Object value : db.values()) {
            try {
                users.add(mapper.readValue(value.toString(), User.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    // метод удаления для админки
    public void deleteByLogin(String login) {
        db.remove(login);
    }

    public StorageService getStorageService() {
        return storageService;
    }
}
