package org.example.domain;

public class GPUProcess {
    private final int pid;
    private final String processName;
    private final String ownerLogin;
    private int usedMemory;

    GPUProcess(int pid, String processName, int usedMemory){
        this.pid = pid;
        this.processName = processName;
        ownerLogin = "owner";
        this.usedMemory = usedMemory;
    }

    @Override
    public String toString() {
        return "GPUProcess{" +
                "pid=" + pid +
                ", processName='" + processName + '\'' +
                ", ownerLogin='" + ownerLogin + '\'' +
                ", usedMemory=" + usedMemory +
                '}';
    }
}
