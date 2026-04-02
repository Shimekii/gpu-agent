package org.example;
import org.example.service.GPUAgent;

import java.util.Arrays;
import java.util.Scanner;
import org.example.service.UserService;
import org.example.repository.UserRepository;
import org.example.domain.User;
import org.example.domain.UserRole;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final GPUAgent agent = new GPUAgent();

    private static final UserRepository userRepository = new UserRepository(agent.getStorageService());
    private static final UserService userService = new UserService(userRepository);

    public static void main(String[] args) {
        initDefaultAdmin(); // инициализация админа по умолчанию, тк чтобы можно было войти в систему, в базе должен быть хотя бы один администратор
        while(true){
            String line = scanner.nextLine().trim();
            String[] commandArgs = line.split("\\s+");
            if (commandArgs.length == 0 || commandArgs[0].isEmpty()) continue;

            switch (commandArgs[0]){
                case "start":
                    agent.startMonitoring();
                    break;
                case "stop":
                    agent.stopMonitoring();
                    break;
                case "show":
                    agent.showData();
                    break;
                case "exit":
                    agent.close();
                    System.exit(0);
                default:
                    System.out.println("Unknown command");
            }

        }
    }

    private static void initDefaultAdmin() {
        try {
            if (userRepository.findByLogin("admin") == null) {
                User admin = new User("admin", "admin123", "System Admin", UserRole.ADMIN);
                userService.createNewUser(admin);
                System.out.println("Админ по умолчанию создан.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void commandShow(String[] args){
        if (args.length == 0) {
            System.out.println("Username is empty");
        }
        else {
            agent.showData();
        }
    }
}