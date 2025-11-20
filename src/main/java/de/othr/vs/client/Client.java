package de.othr.vs.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import your.pkg.Bewertung;
import your.pkg.Messwert;
import your.pkg.SimpleActionServiceGrpc;

public class Client {

  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 1234).usePlaintext().build();

    var sampleMesswert = Messwert.newBuilder().setSensor("Wasserstand").setValue(9.4).build();

    var stub = SimpleActionServiceGrpc.newStub(channel);

    stub.getRequiredAction(
        sampleMesswert,
        new StreamObserver<>() {
          @Override
          public void onCompleted() {}

          @Override
          public void onError(Throwable t) {}

          @Override
          public void onNext(Bewertung b) {
            System.out.println(b);
          }
        });

    var blockingStub = SimpleActionServiceGrpc.newBlockingStub(channel);

    var res = blockingStub.getRequiredAction(sampleMesswert);
    System.out.println(res);
    channel.awaitTermination(30L, TimeUnit.SECONDS);
  }
}
