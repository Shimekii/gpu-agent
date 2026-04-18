package org.example.domain;

import java.time.LocalDateTime;

public class Request {
    private String id;
    private String userLogin;
    private String purpose;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RequestStatus status;
    private LocalDateTime createdAt;

    public Request() {}

    public Request(String id, String userLogin, String purpose, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime createdAt) {
        this.id = id;
        this.userLogin = userLogin;
        this.purpose = purpose;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.status = RequestStatus.CREATED; // по умолчанию создана
    }

    public String getId() { return id; }
    public String getUserLogin() { return userLogin; }
    public String getPurpose() { return purpose; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public RequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setId(String id) { this.id = id; }
    public void setUserLogin(String userLogin) {this.userLogin = userLogin; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
