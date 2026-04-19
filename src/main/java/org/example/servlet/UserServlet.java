package org.example.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.User;
import org.example.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private final UserService userService;
    // Настраиваем маппер сразу при создании
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public UserServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try {
            // Используем JsonNode, чтобы гибко прочитать и объект User, и доп. поля (типа action)
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(req.getInputStream());
            String action = root.has("action") ? root.get("action").asText() : "login";

            // Превращаем часть узла в объект User
            User userData = mapper.treeToValue(root, User.class);

            User existingUser = userService.getUserByLogin(userData.getLogin());

            if ("create".equals(action)) {
                // ЛОГИКА СОЗДАНИЯ (для админки)
                if (existingUser != null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"Пользователь уже существует\"}");
                    return;
                }
                userService.createNewUser(userData);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write("{\"message\": \"User created: " + userData.getLogin() + "\"}");
            } else {
                // ЛОГИКА ВХОДА
                if (existingUser == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Пользователь не найден\"}");
                } else if (userService.authenticate(userData.getLogin(), userData.getPassword())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Login successful\", \"user\": \"" + userData.getLogin() + "\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Неверный пароль\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Чтобы видеть реальную причину в консоли IDE
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // Отправляем ошибку именно в формате JSON
            resp.getWriter().write("{\"error\": \"Ошибка сервера: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String login = req.getParameter("login");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        if (login == null) {
            List<User> users = userService.getAllUsers();
            resp.getWriter().write(mapper.writeValueAsString(users));
        } else {
            User user = userService.getUserByLogin(login);
            resp.getWriter().write(mapper.writeValueAsString(user));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String login = req.getParameter("login");
        resp.setContentType("application/json;charset=UTF-8");

        if (login == null || login.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Логин не указан\"}");
            return;
        }

        try {
            // Проверяем, существует ли пользователь перед удалением
            if (userService.getUserByLogin(login) == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Пользователь не найден\"}");
                return;
            }

            userService.deleteUser(login);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Пользователь " + login + " успешно удален\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Ошибка при удалении: " + e.getMessage() + "\"}");
        }
    }
}