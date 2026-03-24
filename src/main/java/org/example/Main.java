package org.example;
import org.example.service.GPUAgent;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        GPUAgent agent = new GPUAgent();
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
                    agent.showData("owner");
                    break;
                case "exit":
                    agent.close();
                    System.exit(0);
            }
        }
    }
}