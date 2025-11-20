package de.othr.vs.server;

import com.google.protobuf.Timestamp;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import your.pkg.Bewertung;
import your.pkg.Messwert;
import your.pkg.SimpleActionServiceGrpc.SimpleActionServiceImplBase;
import your.pkg.StreamActionServiceGrpc;

class SimpleActionServiceImpl extends SimpleActionServiceImplBase {
  public static Bewertung b =
      Bewertung.newBuilder()
          .setAction("Do something")
          .setTime(
              Timestamp.newBuilder()
                  .setSeconds(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
                  .build())
          .build();

  @Override
  public void getRequiredAction(Messwert request, StreamObserver<Bewertung> responseObserver) {

    System.out.println(request);
    responseObserver.onNext(b);
    responseObserver.onCompleted();
  }
}

class StreamActionServiceImpl extends StreamActionServiceGrpc.StreamActionServiceImplBase {
  @Override
  public StreamObserver<Messwert> streamRequiredAction(StreamObserver<Bewertung> responseObserver) {
    return new StreamObserver<>() {
      double previousWaterLevel = 0;

      @Override
      public void onNext(Messwert messwert) {
        var time =
            Timestamp.newBuilder()
                .setSeconds(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        var action = messwert.getValue() > previousWaterLevel ? "higher" : "lower";

        responseObserver.onNext(Bewertung.newBuilder().setAction(action).setTime(time).build());
        previousWaterLevel = messwert.getValue();
      }

      @Override
      public void onError(Throwable throwable) {}

      @Override
      public void onCompleted() {}
    };
  }
}

public class MesswertServer {

  public static void main(String[] args) throws IOException, InterruptedException {
    var server =
        ServerBuilder.forPort(1234)
            .addService(new SimpleActionServiceImpl())
            .addService(new StreamActionServiceImpl())
            .build();

    server.start();

    System.out.println("Server running...");

    server.awaitTermination();
  }
}
