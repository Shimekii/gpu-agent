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

// аннотация говорит серверу, что этот класс отвечает за адрес /users
@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserServlet(UserService userService) {
        this.userService = userService;
    }

    // метод POST — для создания пользователей (администратором)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // читаем JSON из тела запроса и превращаем в объект User
            User newUser = mapper.readValue(req.getInputStream(), User.class);

            userService.createNewUser(newUser);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("User created: " + newUser.getLogin());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }

    // метод GET — можно использовать для проверки идентификации
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("User management system is active");
    }
}
