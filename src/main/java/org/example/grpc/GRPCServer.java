package org.example.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.service.GPUMonitoringService;
import org.example.service.StorageService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GRPCServer {
    private static final Logger logger = Logger.getLogger(GRPCServer.class.getName());
    private final int port;
    private final Server server;

    public GRPCServer(int port, StorageService storageService) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new GPUMonitoringService(storageService))
                .build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("GRPC server started on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down GRPC server");
            try {
                GRPCServer.this.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }
}
