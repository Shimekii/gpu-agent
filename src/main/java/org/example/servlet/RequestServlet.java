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
        String login = req.getParameter("login");
        String action = req.getParameter("action");

        try {
            List<Request> allRequests = repository.findAll();

            if ("checkStatus".equals(action)) {
                LocalDateTime now = LocalDateTime.now();
                Request nearest = allRequests.stream()
                        .filter(r -> r.getUserLogin().equals(login))
                        // Добавляем CREATED в фильтр, чтобы видеть новую заявку на главной
                        .filter(r -> r.getStatus() == RequestStatus.APPROVED ||
                                r.getStatus() == RequestStatus.ACTIVE ||
                                r.getStatus() == RequestStatus.CREATED)
                        .filter(r -> r.getEndTime().isAfter(now))
                        .min(Comparator.comparing(Request::getStartTime))
                        .orElse(null);

                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(mapper.writeValueAsString(nearest));
            }
            else {
                // Просто список всех заявок пользователя
                List<Request> userRequests = allRequests.stream()
                        .filter(r -> r.getUserLogin().equals(login))
                        .sorted(Comparator.comparing(Request::getStartTime).reversed()) // свежие сверху
                        .collect(Collectors.toList());

                resp.setContentType("application/json");
                resp.getWriter().write(mapper.writeValueAsString(userRequests));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Jackson создаст объект и заполнит те поля, что пришли из JSON (login, purpose, times)
            Request newRequest = mapper.readValue(req.getInputStream(), Request.class);

            // Дозаполняем системные поля, которых нет в форме на фронтенде
            if (newRequest.getId() == null) {
                newRequest.setId(java.util.UUID.randomUUID().toString()); // Генерируем ID
            }
            if (newRequest.getCreatedAt() == null) {
                newRequest.setCreatedAt(LocalDateTime.now()); // Ставим дату создания
            }
            if (newRequest.getStatus() == null) {
                newRequest.setStatus(RequestStatus.CREATED); // Начальный статус
            }

            repository.save(newRequest);
            resp.setStatus(201);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"status\":\"created\"}");
        } catch (Exception e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}