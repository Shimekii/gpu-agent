package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.*;
import org.example.repository.*;
import java.time.LocalDateTime;
import java.util.Map;

public class CommandProcessor {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final GPUAgent agent; // Добавили агента сюда
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public CommandProcessor(UserService userService, UserRepository userRepository, RequestRepository requestRepository, GPUAgent agent) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.agent = agent;
    }

    public void startAgent() { agent.startMonitoring(); }
    public void stopAgent() { agent.stopMonitoring(); }
    public void showAgentData() { agent.showData(); }

    public void showUsers() {
        try {
            System.out.println("\n--- ЗАРЕГИСТРИРОВАННЫЕ ПОЛЬЗОВАТЕЛИ ---");
            Map<String, Object> users = userRepository.getStorageService().getStore().openMap("userMap");
            if (users.isEmpty()) System.out.println("Список пуст");
            users.forEach((k, v) -> System.out.println(k + " => " + v));
        } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    public void showLogs() {
        try {
            System.out.println("\n--- ИСТОРИЯ ПОДКЛЮЧЕНИЙ ---");
            Map<String, Object> logs = userRepository.getStorageService().getStore().openMap("connectionLogMap");
            if (logs.isEmpty()) System.out.println("Логов пока нет");
            logs.forEach((k, v) -> System.out.println(v)); // Выводим JSON строку лога
        } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    public void createTestRequest(String login) {
        try {
            if (userRepository.findByLogin(login) == null) {
                System.out.println("Ошибка: Сначала создайте пользователя через Postman!");
                return;
            }
            String id = "req_" + System.currentTimeMillis();
            Request req = new Request(id, login, "Test GPU Work",
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusHours(2),
                    LocalDateTime.now());
            requestRepository.save(req);
            System.out.println("Заявка " + id + " создана! (статус CREATED)");
        } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    public void approveAll() {
        try {
            var requests = requestRepository.findAll();
            for (Request r : requests) {
                r.setStatus(RequestStatus.APPROVED);
                requestRepository.save(r);
            }
            System.out.println("Все заявки (" + requests.size() + ") одобрены.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void showRequests() {
        try {
            System.out.println("--- СПИСОК ЗАЯВОК ---");
            var reqs = requestRepository.findAll();
            if (reqs.isEmpty()) System.out.println("Заявок нет");
            reqs.forEach(r -> System.out.println("[" + r.getStatus() + "] User: " + r.getUserLogin() + " ID: " + r.getId()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void printHelp() {
        System.out.println("\n=== ДОСТУПНЫЕ КОМАНДЫ ===");
        System.out.println("help                     - показать это меню");
        System.out.println("login <login>            - имитация входа пользователя");
        System.out.println("start/stop/show          - управление GPU агентом");
        System.out.println("create-request <login>   - создать заявку (CREATED)");
        System.out.println("approve-requests         - одобрить все заявки (ADMIN)");
        System.out.println("show-requests            - список заявок и их статусов");
        System.out.println("show-users               - список всех пользователей");
        System.out.println("show-logs                - история всех подключений");
        System.out.println("delete <login>           - удалить пользователя из системы");
        System.out.println("clear-requests           - очистить хранилище заявок");
        System.out.println("clear                    - очистить хранилище (заявки и логи)");
        System.out.println("exit                     - выход\n");
    }

    public void clearAllData() {
        try {
            // очищаем все map в базе данных
            userRepository.getStorageService().getStore().openMap("requestMap").clear();
            userRepository.getStorageService().getStore().openMap("connectionLogMap").clear();
            userRepository.getStorageService().getStore().commit();
            System.out.println("База данных полностью очищена (заявки и логи)!");
        } catch (Exception e) {
            System.out.println("Ошибка при очистке: " + e.getMessage());
        }
    }

    public void showLogsFriendly() {
        try {
            System.out.println("\n=== ЖУРНАЛ ПОДКЛЮЧЕНИЙ ===");
            Map<String, Object> logs = userRepository.getStorageService().getStore().openMap("connectionLogMap");
            if (logs.isEmpty()) System.out.println("Журнал пуст.");

            for (Object v : logs.values()) {
                // читаем из JSON в объект, чтобы достать поля
                ConnectionLog log = mapper.readValue(v.toString(), ConnectionLog.class);
                // выводим в одну строку красиво и читабельно
                System.out.printf("Пользователь: %-15s | Статус: %-20s%n",
                        log.getId(), // или log.getLogin()
                        log.getConnectionType());
            }
        } catch (Exception e) {
            System.out.println("Ошибка вывода логов: " + e.getMessage());
        }
    }

    public void deleteUser(String login) {
        try {
            Map<String, Object> users = userRepository.getStorageService().getStore().openMap("userMap");
            if (users.remove(login) != null) {
                userRepository.getStorageService().getStore().commit();
                System.out.println("Пользователь " + login + " удален из системы.");
            } else {
                System.out.println("Пользователь не найден.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при удалении: " + e.getMessage());
        }
    }

    public void clearRequests() {
        try {
            requestRepository.getStorageService().getStore().openMap("requestMap").clear();
            requestRepository.getStorageService().getStore().commit();
            System.out.println("Все заявки удалены.");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
