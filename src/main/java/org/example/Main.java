package org.example;
import org.example.service.GPUAgent;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.example.service.UserService;
import org.example.repository.UserRepository;
import org.example.domain.User;
import org.example.domain.Request;
import org.example.domain.RequestStatus;
import java.time.LocalDateTime;
import org.example.domain.UserRole;
import org.example.repository.RequestRepository;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.example.servlet.UserServlet;
import java.io.File;
import org.example.service.CommandProcessor;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final GPUAgent agent = new GPUAgent();

    private static final UserRepository userRepository = new UserRepository(agent.getStorageService());

    private static final RequestRepository requestRepository = new RequestRepository(agent.getStorageService());

    private static final UserService userService = new UserService(userRepository, requestRepository);

    private static final CommandProcessor commands = new CommandProcessor(userService, userRepository, requestRepository, agent);

    public static void main(String[] args) {
        try {
            startServer();
        } catch (Exception e) {
            System.err.println("Не удалось запустить HTTP сервер: " + e.getMessage());
        }
        initDefaultAdmin(); // инициализация админа по умолчанию, тк чтобы можно было войти в систему, в базе должен быть хотя бы один администратор
        commands.printHelp();
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
                case "help":
                    commands.printHelp();
                    break;
                case "login":
                    if (commandArgs.length > 1) {
                        try {
                            userService.processLogin(commandArgs[1]);
                        } catch (Exception e) {
                            System.err.println("Ошибка при попытке входа: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Укажите логин: login <username>");
                    }
                    break;
                case "create-request":
                    commands.createTestRequest(commandArgs[1]);
                    break;
                case "approve-requests":
                    commands.approveAll();
                    break;
                case "show-requests":
                    commands.showRequests();
                    break;
                case "show-users":
                    commands.showUsers();
                    break;
                case "show-logs":
                    commands.showLogs();
                    // commands.showLogsFriendly();
                    break;
                case "delete":
                    if (commandArgs.length > 1) {
                        commands.deleteUser(commandArgs[1]);
                    } else {
                        System.out.println("Укажите логин: delete <username>");
                    }
                    break;
                case "clear-requests":
                    commands.clearRequests();
                    break;
                case "clear":
                    commands.clearAllData();
                    break;
                default:
                    System.out.println("Неизвестная команда");
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

    private static void startServer() throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080); // порт для Postman
        tomcat.getConnector();

        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext("", docBase);

        // регистрируем сервлет вручную
        UserServlet userServlet = new UserServlet(userService);
        Tomcat.addServlet(context, "UserServlet", userServlet);
        context.addServletMappingDecoded("/users", "UserServlet");

        tomcat.start();
        System.out.println("HTTP сервер запущен на http://localhost:8080/users");
    }
}