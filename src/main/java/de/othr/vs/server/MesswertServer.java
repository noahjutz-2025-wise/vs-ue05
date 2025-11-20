package de.othr.vs.server;

import com.google.protobuf.Timestamp;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import your.pkg.Bewertung;
import your.pkg.Messwert;
import your.pkg.SimpleActionServiceGrpc.SimpleActionServiceImplBase;

class SimpleActionServiceImpl extends SimpleActionServiceImplBase {

    @Override
    public void getRequiredAction(
        Messwert request,
        StreamObserver<Bewertung> responseObserver
    ) {
        responseObserver.onNext(
            Bewertung.newBuilder()
                .setAction("Do something")
                .setTime(
                    Timestamp.newBuilder()
                        .setSeconds(
                            TimeUnit.MILLISECONDS.toSeconds(
                                System.currentTimeMillis()
                            )
                        )
                        .build()
                )
                .build()
        );
        responseObserver.onCompleted();
    }
}

public class MesswertServer {

    public static void main(String[] args)
        throws IOException, InterruptedException {
        var server = ServerBuilder.forPort(1234)
            .addService(new SimpleActionServiceImpl())
            .build();

        server.start();

        System.out.println("Server running...");

        server.awaitTermination();
    }
}
