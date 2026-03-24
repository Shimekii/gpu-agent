package org.example.domain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GPU {
    private final int id;
    private GpuStatus status;
    private final int totalMemory;

    // конструктор поразумевает одну видеокарту (пока что)
    // при инициализации подтягивает информацию об GPU
    public GPU(){
        ProcessBuilder pb = new ProcessBuilder(
                "nvidia-smi",
                "--query-gpu=index,memory.total",
                "--format=csv,noheader,nounits"
        );
        String[] result = new String[0];
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line = reader.readLine();
            result = line.split(",\\s*");
            process.destroy();

        } catch (Exception e){
            e.printStackTrace();
        }
        id = Integer.parseInt(result[0]);
        totalMemory = Integer.parseInt(result[1]);
        status = GpuStatus.FREE;
    }

    // сбор метрик в текущий момент
    public GPUMetric computeMetrics(){
        ProcessBuilder pb = new ProcessBuilder(
                "nvidia-smi",
                "--query-gpu=memory.free,temperature.gpu,timestamp",
                "--format=csv,noheader,nounits"
        );

        try{
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = reader.readLine();
            String[] result = line.split(",\\s*");
            process.destroy();

            int free = Integer.parseInt(result[0]);
            int temp = Integer.parseInt(result[1]);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
            LocalDateTime ldt = LocalDateTime.parse(result[2], formatter);
            Instant timestamp = ldt
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            return new GPUMetric(timestamp, free, temp);

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // информация об запущенных процессах
    public List<GPUProcess> getProcesses(){
        ProcessBuilder pb = new ProcessBuilder(
                "nvidia-smi",
                "--query-compute-apps=pid,process_name,used_memory",
                "--format=csv,noheader"
        );
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            List<GPUProcess> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null){
                String[] parseLine = line.split(",\\s*");
                int pid = Integer.parseInt(parseLine[0]);
                String name = parseLine[1];
                int usedMemory = 0;
                if (!parseLine[2].equals("[N/A]")){
                    usedMemory = Integer.parseInt(parseLine[2]);
                }
                result.add(new GPUProcess(pid, name, usedMemory));
            }
            process.destroy();
            return result;

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "GPU{" +
                "id=" + id +
                ", status=" + status +
                ", totalMemory=" + totalMemory +
                '}';
    }
}
