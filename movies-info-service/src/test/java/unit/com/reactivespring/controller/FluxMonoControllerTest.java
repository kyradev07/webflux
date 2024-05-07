package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(controllers = FluxMonoController.class)
@AutoConfigureWebTestClient
class FluxMonoControllerTest {

    @Autowired
    public WebTestClient webClient;


    @Test
    void flux() {
        webClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .hasSize(3);
    }

    @Test
    void flux2() {
        Flux<Integer> responseBody = webClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Integer.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void flux3() {
        webClient.get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Integer.class)
                .consumeWith(listEntityExchangeResult -> {
                    assert Objects.requireNonNull(listEntityExchangeResult.getResponseBody()).size() == 3;
                });
    }

    @Test
    void mono() {
        webClient.get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Hello World From Mono Reactive", responseBody);
                });
    }

    @Test
    void stream() {
        Flux<Long> responseBody = webClient.get()
                .uri("/stream")
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Long.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectNext(0L, 1L, 2L, 3L)
                .thenCancel()
                .verify();
    }
}