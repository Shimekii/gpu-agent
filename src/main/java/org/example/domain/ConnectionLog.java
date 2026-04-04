package org.example.domain;

import java.time.Instant;

public class ConnectionLog {
    private String id;
    private Instant loginTime;
    private Instant logoutTime;
    private ConnectionType connectionType;

    public ConnectionLog() {
    }

    public ConnectionLog(String id){
        this.id = id;
        loginTime = Instant.now();
        connectionType = ConnectionType.IN_TIME;
    }

    public String getId() { return id; }
    public Instant getLoginTime() { return loginTime; }
    public Instant getLogoutTime() { return logoutTime; }
    public ConnectionType getConnectionType() { return connectionType; }
    public void setConnectionType(ConnectionType connectionType) { this.connectionType = connectionType; }
    public void setId(String id) { this.id = id; }
    public void setLoginTime(Instant loginTime) { this.loginTime = loginTime; }
    public void setLogoutTime(Instant logoutTime) { this.logoutTime = logoutTime; }

    public void closeConnection(){
        logoutTime = Instant.now();
    }
}
