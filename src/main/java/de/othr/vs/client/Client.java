package de.othr.vs.client;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import your.pkg.Bewertung;
import your.pkg.Messwert;
import your.pkg.SimpleActionServiceGrpc;
import your.pkg.StreamActionServiceGrpc;

import javax.security.auth.callback.CallbackHandler;

public class Client {
  public static Messwert sampleMesswert =
      Messwert.newBuilder().setSensor("Wasserstand").setValue(9.4).build();

  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 1234).usePlaintext().build();

    System.out.println("simpleBlocking");
    simpleBlocking(channel);
    System.out.println("simpleCallback");
    simpleCallback(channel);
    System.out.println("streamBlocking -- UNSUPPORTED");
    System.out.println("streamCallback");
    streamCallback(channel);

    channel.awaitTermination(30L, TimeUnit.SECONDS);
  }

  private static void simpleBlocking(Channel channel) {
    var blockingStub = SimpleActionServiceGrpc.newBlockingStub(channel);

    var res = blockingStub.getRequiredAction(sampleMesswert);
    System.out.println(res);
  }

  private static void simpleCallback(Channel channel) {
    var stub = SimpleActionServiceGrpc.newStub(channel);

    stub.getRequiredAction(
        sampleMesswert,
        new StreamObserver<>() {
          @Override
          public void onCompleted() {}

          @Override
          public void onError(Throwable t) {}

          @Override
          public void onNext(Bewertung bewertung) {
            System.out.println("Msg from server: " + bewertung);
          }
        });
  }

  private static void streamCallback(Channel channel) {
    var streamStub = StreamActionServiceGrpc.newStub(channel);

    var handle =
        streamStub.streamRequiredAction(
            new StreamObserver<>() {
              @Override
              public void onNext(Bewertung bewertung) {
                System.out.println("Msg from server: " + bewertung);
              }

              @Override
              public void onError(Throwable throwable) {}

              @Override
              public void onCompleted() {}
            });

    handle.onNext(sampleMesswert);
    handle.onNext(sampleMesswert);
    handle.onNext(sampleMesswert);
  }
}
