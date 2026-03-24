package org.example;
import org.example.service.GPUAgent;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final GPUAgent agent = new GPUAgent();
    public static void main(String[] args) {
        boolean running = true;
        while(running){
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
                    commandShow(Arrays.copyOfRange(commandArgs, 1, commandArgs.length));
                    break;
                case "exit":
                    agent.close();
                    System.exit(0);
            }
        }
    }

    private static void commandShow(String[] args){
        if (args.length == 0) {
            System.out.println("Username is empty");
        }
        else {
            agent.showData(args[0]);
        }
    }
}