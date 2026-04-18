package org.example.service;

import org.example.domain.User;
import org.example.repository.UserRepository;
import org.example.repository.RequestRepository;
import org.example.domain.ConnectionType;
import org.example.domain.ConnectionLog;
import java.time.LocalDateTime;
import org.example.domain.RequestStatus;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserService {
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    public UserService(UserRepository userRepository, RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    public boolean authenticate(String login, String password) throws Exception {
        User user = userRepository.findByLogin(login);
        if (user == null) return false;
        // сравниваем строки напрямую
        return user.getPassword().equals(password);
    }

    public void createNewUser(User user) throws Exception {
        if (userRepository.findByLogin(user.getLogin()) != null) {
            throw new IllegalArgumentException("Пользователь уже существует!");
        }
        userRepository.save(user);
    }

    public boolean isUserInWorkTime(String login) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        return requestRepository.findAll().stream()
                .filter(r -> r.getUserLogin().equals(login))
                // проверяем, что заявка либо одобрена, либо уже активна
                .filter(r -> r.getStatus() == RequestStatus.APPROVED || r.getStatus() == RequestStatus.ACTIVE)
                // проверка временного окна
                .anyMatch(r -> now.isAfter(r.getStartTime()) && now.isBefore(r.getEndTime()));
    }

    public void processLogin(String login) throws Exception {
        // проверка
        if (userRepository.findByLogin(login) == null) {
            System.out.println("Ошибка: Пользователь " + login + " не найден в системе!");
            return;
        }

        // проверяем время (пока не знаем, занята ли GPU (это к задаче 2 относится часть))
        boolean inTime = isUserInWorkTime(login);

        // создаем объект лога
        ConnectionLog log = new ConnectionLog(login);
        log.setConnectionType(inTime ? ConnectionType.IN_TIME : ConnectionType.OUT_OF_TIME_NO_CONFLICT);

        // сохраняем в MVStore в виде JSON строки
        Map<String, Object> logMap = userRepository.getStorageService().getStore().openMap("connectionLogMap");

        // используем ObjectMapper для записи объекта
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        logMap.put(login + "_" + System.currentTimeMillis(), mapper.writeValueAsString(log));
        userRepository.getStorageService().getStore().commit();

        System.out.println("Событие входа залогировано для: " + login);
    }
}
