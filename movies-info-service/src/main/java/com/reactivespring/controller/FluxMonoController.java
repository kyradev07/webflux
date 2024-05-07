package com.reactivespring.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxMonoController {

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        return Flux.just(1, 2, 3).log();
    }

    @GetMapping("/mono")
    public Mono<String> mono() {
        return Mono.just("Hello World From Mono Reactive").log();
    }

    //Streaming Endpoint: continuously sends updates to clients as the new data arrives.
    //You need to specify the type of the media type. MediaType.TEXT_EVENT_STREAM_VALUE
    //Server Sent Events - SSE
    //Example: Stock Tickers, Realtime updates of Sport Events

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream() {
        return Flux.interval(Duration.ofSeconds(1)).log();
    }
}
