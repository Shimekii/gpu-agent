package org.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.User;
import org.example.service.StorageService;
import java.util.Map;

public class UserRepository {
    private final Map<String, Object> db;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserRepository(StorageService storageService) {
        // отдельный map для пользователей
        this.db = storageService.getStore().openMap("userMap");
    }

    public void save(User user) throws Exception {
        db.put(user.getLogin(), mapper.writeValueAsString(user));
    }

    public User findByLogin(String login) throws Exception {
        Object data = db.get(login);
        if (data == null) return null;
        return mapper.readValue(data.toString(), User.class);
    }
}
