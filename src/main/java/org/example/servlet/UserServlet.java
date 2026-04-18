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

@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserServlet(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            // Читаем входящий JSON (логин и пароль)
            User userData = mapper.readValue(req.getInputStream(), User.class);

            // Пытаемся найти пользователя в базе
            User existingUser = null;
            try {
                // Если userService.authenticate возвращает true, значит пароль верный
                if (userService.authenticate(userData.getLogin(), userData.getPassword())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Login successful\", \"user\": \"" + userData.getLogin() + "\"}");
                    return;
                } else {
                    // Если зашли сюда, значит логин есть, но пароль неверный
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Неверный пароль\"}");
                    return;
                }
            } catch (Exception e) {
                // Если authenticate выкинул ошибку или юзер не найден, пробуем его создать
                // (это сохраняет старую логику регистрации)
                try {
                    userService.createNewUser(userData);
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    resp.getWriter().write("{\"message\": \"User created: " + userData.getLogin() + "\"}");
                } catch (Exception ex) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"" + ex.getMessage() + "\"}");
                }
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Ошибка сервера: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("User management system is active");
    }
}