package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.Request;
import org.example.domain.RequestStatus;
import org.example.repository.RequestRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/requests")
public class RequestServlet extends HttpServlet {
    private final RequestRepository repository;
    private final ObjectMapper mapper;

    public RequestServlet(RequestRepository repository) {
        this.repository = repository;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String login = req.getParameter("login");
        String action = req.getParameter("action");
        String requestId = req.getParameter("id");

        try {
            if ("listAll".equals(action)) {
                List<Request> all = repository.findAll().stream()
                        .sorted(Comparator.comparing(Request::getCreatedAt).reversed())
                        .collect(Collectors.toList());
                resp.getWriter().write(mapper.writeValueAsString(all));
            } else if ("get".equals(action) && requestId != null) {
                Request r = repository.findById(requestId);
                resp.getWriter().write(mapper.writeValueAsString(r));
            } else if ("checkStatus".equals(action)) {
                LocalDateTime now = LocalDateTime.now();
                // ИСПРАВЛЕНО: берем данные напрямую из репозитория
                Request nearest = repository.findAll().stream()
                        .filter(r -> r.getUserLogin().equals(login))
                        .filter(r -> r.getStatus() == RequestStatus.APPROVED ||
                                r.getStatus() == RequestStatus.ACTIVE ||
                                r.getStatus() == RequestStatus.CREATED)
                        .filter(r -> r.getEndTime().isAfter(now))
                        .min(Comparator.comparing(Request::getStartTime))
                        .orElse(null);

                resp.getWriter().write(mapper.writeValueAsString(nearest));
            } else {
                List<Request> userRequests = repository.findAll().stream()
                        .filter(r -> r.getUserLogin().equals(login))
                        .sorted(Comparator.comparing(Request::getStartTime).reversed())
                        .collect(Collectors.toList());
                resp.getWriter().write(mapper.writeValueAsString(userRequests));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try {
            // 1. Читаем JSON в дерево один раз
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(req.getInputStream());

            // Проверяем, есть ли action (для админа)
            String action = root.has("action") ? root.get("action").asText() : "create";

            if ("updateStatus".equals(action)) {
                // ЛОГИКА АДМИНА: Обновление статуса
                String id = root.get("id").asText();
                RequestStatus newStatus = RequestStatus.valueOf(root.get("status").asText());

                Request r = repository.findById(id);
                if (r != null) {
                    r.setStatus(newStatus);
                    repository.save(r);
                    resp.getWriter().write("{\"message\":\"Status updated\"}");
                }
            } else {
                // ЛОГИКА СТУДЕНТА: Создание новой заявки
                // Превращаем JsonNode в объект Request
                Request newRequest = mapper.treeToValue(root, Request.class);

                // Дозаполняем системные поля
                if (newRequest.getId() == null) {
                    newRequest.setId(java.util.UUID.randomUUID().toString());
                }
                if (newRequest.getCreatedAt() == null) {
                    newRequest.setCreatedAt(LocalDateTime.now());
                }
                if (newRequest.getStatus() == null) {
                    newRequest.setStatus(RequestStatus.CREATED);
                }

                repository.save(newRequest);
                resp.setStatus(201);
                resp.getWriter().write("{\"status\":\"created\"}");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Чтобы видеть ошибку в консоли IDE
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}