package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.ConnectionLog;
import org.example.service.StorageService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/logs")
public class LogServlet extends HttpServlet {
    private final StorageService storageService;
    private final ObjectMapper mapper;

    public LogServlet(StorageService storageService) {
        this.storageService = storageService;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        String logId = req.getParameter("id"); // Например, login_timestamp

        Map<String, Object> logMap = storageService.getStore().openMap("connectionLogMap");

        try {
            if ("get".equals(action) && logId != null) {
                // Детали одного лога
                Object data = logMap.get(logId);
                if (data != null) {
                    resp.getWriter().write(data.toString());
                } else {
                    resp.setStatus(404);
                }
            } else {
                // Список всех логов
                List<Map<String, Object>> logs = new ArrayList<>();
                for (Map.Entry<String, Object> entry : logMap.entrySet()) {
                    // Распаковываем JSON, чтобы добавить ID ключа для фронтенда
                    Map<String, Object> logObj = mapper.readValue(entry.getValue().toString(), Map.class);
                    logObj.put("internalId", entry.getKey()); // сохраняем ключ мапы как ID
                    logs.add(logObj);
                }
                // Сортируем: новые сверху (по ключу timestamp в конце)
                logs.sort((a, b) -> b.get("internalId").toString().compareTo(a.get("internalId").toString()));
                resp.getWriter().write(mapper.writeValueAsString(logs));
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}