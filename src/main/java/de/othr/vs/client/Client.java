package de.othr.vs.client;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import your.pkg.Bewertung;
import your.pkg.Messwert;
import your.pkg.SimpleActionServiceGrpc;
import your.pkg.StreamActionServiceGrpc;

import javax.security.auth.callback.CallbackHandler;

public class Client {
  public static Messwert sampleMesswert =
      Messwert.newBuilder().setSensor("Wasserstand").setValue(0.1).build();
  public static Messwert sampleMesswert2 =
      Messwert.newBuilder().setSensor("Wasserstand").setValue(0.2).build();
  public static Messwert sampleMesswert3 =
      Messwert.newBuilder().setSensor("Wasserstand").setValue(0.3).build();

  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 1234).usePlaintext().build();

    // System.out.println("simpleBlocking");
    // simpleBlocking(channel);
    // System.out.println("simpleCallback");
    // simpleCallback(channel);
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

  private static void streamCallback(Channel channel) throws InterruptedException {
    var random = new Random();
    var streamStub = StreamActionServiceGrpc.newStub(channel);

    var handle =
        streamStub.streamRequiredAction(
            new StreamObserver<>() {
              int damHeight = 0;

              @Override
              public void onNext(Bewertung bewertung) {
                damHeight =
                    switch (bewertung.getAction()) {
                      case "lower" -> damHeight > 0 ? damHeight - 1 : 0;
                      case "higher" -> damHeight < 1000 ? damHeight + 1 : 1000;
                      default -> damHeight;
                    };
                System.out.println("Dam height: " + damHeight);
              }

              @Override
              public void onError(Throwable throwable) {}

              @Override
              public void onCompleted() {}
            });

    for (int i = 0; i < 1000; i++) {
      var x = random.nextInt(1000);
      System.out.println("Wasserstand: " + x);
      var measurement = Messwert.newBuilder().setSensor("Wasserstand").setValue(x).build();
      handle.onNext(measurement);
      Thread.sleep(3000);
    }
  }
}
