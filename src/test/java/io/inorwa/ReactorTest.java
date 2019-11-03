package io.inorwa;

import org.junit.After;
import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.inorwa.Utils.*;

public class ReactorTest {
    
    @After
    public void after() {
        sleep(10_000);
    }
    
    @Test
    public void interval() throws InterruptedException {
        Flux.interval(Duration.ofSeconds(1)).subscribe(System.out::println);
        
        TimeUnit.SECONDS.sleep(3);
    }
    
    @Test
    public void synchronusGenerationTest() {
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return state;
                }
        );
        
        flux.subscribe(System.out::println);
    }
    
    public static <T> Flux<T> createThreadFlux(Supplier<T> supplier, int sleepInterval) {
        return Flux.create(fluxSink -> {
            Runnable task = () -> {
                while (true) {
                    System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + ": Emit value");
                    fluxSink.next(supplier.get());
                    try {
                        TimeUnit.SECONDS.sleep(sleepInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
//            new Thread(task).start();
            Executors.defaultThreadFactory().newThread(task).start();
        });
    }
    
    public <T> void subscribeAsync(Flux<T> flux, Consumer<T> consumer) {
        Runnable task = () -> {
            flux.subscribe(consumer);
        };
        new Thread(task).start();
        Executors.defaultThreadFactory().newThread(task).start();
    }
    
    @Test
    public void createTrain() throws InterruptedException {
        Flux<Object> sampleFlux = createThreadFlux(() -> LocalDateTime.now().format(DateTimeFormatter.ISO_TIME), 1);
        ConnectableFlux<Object> connectableFlux = sampleFlux.publish();
        connectableFlux.connect();
        TimeUnit.SECONDS.sleep(1);
        subscribeAsync(sampleFlux, val -> System.out.println("first: " + val));
        TimeUnit.SECONDS.sleep(1);
        System.out.println("after sleep");
        subscribeAsync(sampleFlux, val -> System.out.println("second: " + val));
    }
    
    
    @Test
    public void test3() {
        Flux fastClock = Flux.interval(Duration.ofSeconds(1)).map(tick -> "fast:" + tick);
        Flux slowClock = Flux.interval(Duration.ofSeconds(2)).map(tick -> "slow:" + tick);
        
        Flux clock = Flux.merge(
                fastClock.filter(tick -> isFastTime()),
                slowClock.filter(tick -> isSlowTime())
        );

//        clock.subscribe(System.out::println);
        
        Flux feed = Flux.interval(Duration.ofSeconds(1)).map(tick -> LocalDateTime.now());
        
        clock.withLatestFrom(feed, (tick, time) -> tick + " " + time).subscribe(System.out::println);
    }
    
    @Test
    public void test5() {
        SomeFeed feed = new SomeFeed();
        Flux feedFlux = Flux.create(emitter -> {
            feed.register(new SomeListener() {
                @Override
                public void priceTick(PriceTick event) {
                    emitter.next(event);
                }
                @Override
                public void error(Throwable throwable) {
                    emitter.error(throwable);
                }
            });
        }, FluxSink.OverflowStrategy.LATEST);
        ConnectableFlux connectableFlux = feedFlux.publish();
        connectableFlux.connect();
        feedFlux.subscribe(System.out::println);
    }
    
}