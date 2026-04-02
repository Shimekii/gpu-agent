package org.example.service;

import org.example.domain.User;
import org.example.repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
