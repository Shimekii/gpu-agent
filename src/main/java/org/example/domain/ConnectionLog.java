package org.example.domain;

import java.time.Instant;

public class ConnectionLog {
    private final String id;
    private final Instant loginTime;
    private Instant logoutTime;
    private ConnectionType connectionType;

    public ConnectionLog(String id){
        this.id = id;
        loginTime = Instant.now();
        logoutTime = null;
        connectionType = ConnectionType.IN_TIME;
    }

    public String getId() { return id; }
    public Instant getLoginTime() { return loginTime; }
    public Instant getLogoutTime() { return logoutTime; }
    public ConnectionType getConnectionType() { return connectionType; }

    public void closeConnection(){
        logoutTime = Instant.now();
    }
}
