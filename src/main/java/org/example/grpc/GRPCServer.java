package org.example.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.domain.GPUMetric;
import org.example.service.GPUAgent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GRPCServer {
    private static final Logger logger = Logger.getLogger(GRPCServer.class.getName());
    private final int port;
    private final Server server;

    public GRPCServer(int port, GPUAgent agent) {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                .addService(new GPUMonitoringGrpc.GPUMonitoringImplBase() {
                    @Override
                    public void getLatestMetric(Empty request, StreamObserver<GPUMetricData> responseObserver) {
                        try {
                            agent.getLatestMetric()
                                    .ifPresentOrElse(
                                            metric -> {
                                                responseObserver.onNext(convertToProto(metric, agent));
                                                responseObserver.onCompleted();
                                            },
                                            () -> responseObserver.onError(Status.NOT_FOUND.asException())
                                    );
                        } catch (Exception e) {
                            logger.severe("Error in getLatestMetric: " + e.getMessage());
                            responseObserver.onError(Status.INTERNAL.asException());
                        }
                    }
                    public void getAllMetrics(Empty request, StreamObserver<GPUMetricData> responseObserver) {
                        try {
                            agent.getAllMetrics()
                                    .map(metric -> convertToProto(metric, agent))
                                    .forEach(responseObserver::onNext);
                            responseObserver.onCompleted();
                        } catch (Exception e) {
                            logger.severe("Error in getAllMetrics");
                            responseObserver.onError(Status.INTERNAL.asException());
                        }
                    }
                    public GPUMetricData convertToProto(GPUMetric metric, GPUAgent agent) {
                        return GPUMetricData.newBuilder()
                                .setTimestamp(metric.getTimestamp().toString())
                                .setFreeMemoryMb(metric.getFreeMemory())
                                .setTemperatureC(metric.getTemperature())
                                .addAllProcesses(agent.getCurrentProcesses())
                                .build();
                    }
                })
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
