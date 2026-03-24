package org.example;


import org.example.domain.GPU;
import org.example.domain.GPUProcess;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        GPU gpu = new GPU();
        System.out.println(gpu);
        System.out.println(gpu.computeMetrics());
        List<GPUProcess> processes = gpu.getProcesses();

        for (GPUProcess process : processes){
            System.out.println(process);
        }
    }
}