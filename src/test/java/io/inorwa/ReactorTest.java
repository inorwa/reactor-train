package io.inorwa;

import org.junit.After;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.inorwa.Utils.sleep;

public class ReactorTest {
    @Test
    public void interval() throws InterruptedException {
        Flux.interval(Duration.ofSeconds(1)).subscribe(x -> System.out.println(x));
    
        TimeUnit.SECONDS.sleep(3);
    }
    
    @After
    public void after() {
        sleep(10_000);
    }
    
    @Test
    public void test3() throws ExecutionException, InterruptedException {
        final CompletableFuture promise = new CompletableFuture();
        Flux fastClock = Flux.interval(Duration.ofSeconds(1)).map(tick -> "fast:" + tick);
        Flux slowClock = Flux.interval(Duration.ofSeconds(2)).map(tick -> "slow:" + tick);
        
        Flux clock = Flux.merge(slowClock,fastClock);
        
        fastClock.subscribe(System.out::println);
        promise.get();
    }
    
    @Test
    public void test5(){
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
        });
        
        feedFlux.subscribe(System.out::println);
    }
    
}