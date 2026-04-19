package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.GPUMetric;
import org.example.service.GPUAgent;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/gpu")
public class GpuServlet extends HttpServlet {
    private final GPUAgent agent;
    private final ObjectMapper mapper = new ObjectMapper();

    public GpuServlet(GPUAgent agent) {
        this.agent = agent;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String action = req.getParameter("action");

        // ВАЖНО: берем ту мапу, в которую пишет GPUAgent
        Map<String, Object> dataMap = agent.getStorageService().getMap();

        try {
            if ("history".equals(action)) {
                List<Map<String, Object>> history = new ArrayList<>();

                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    try {
                        // Пытаемся распарсить JSON из базы
                        Map<String, Object> snapshot = mapper.readValue(entry.getValue().toString(), Map.class);
                        snapshot.put("id", entry.getKey()); // Используем ключ (дату) как ID
                        history.add(snapshot);
                    } catch (Exception e) {
                        // Пропускаем, если в этой мапе лежит что-то другое (например, не JSON)
                    }
                }

                // Сортируем: свежие сверху
                history.sort((a, b) -> b.get("id").toString().compareTo(a.get("id").toString()));
                resp.getWriter().write(mapper.writeValueAsString(history));

            } else {
                // Текущие данные (берем самый свежий объект из мапы)
                if (dataMap.isEmpty()) {
                    resp.getWriter().write("{}");
                    return;
                }

                String lastKey = dataMap.keySet().stream().max(String::compareTo).orElse(null);
                Object lastData = dataMap.get(lastKey);

                // Отправляем содержимое последнего снимка (там внутри есть поля metrics и processes)
                resp.getWriter().write(lastData.toString());
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}