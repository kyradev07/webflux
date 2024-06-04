package com.reactivespring.controller;

import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8083)
@TestPropertySource(properties = {
        "restClient.movieInfoUrl=http://localhost:8083/v1/movie-info",
        "restClient.reviewUrl=http://localhost:8083/v1/review",
})
public class MoviesControllerIntgTest {

    @Autowired
    public WebTestClient webClient;


    @Test
    void retrieveMovieById() {

        String id = "1";

        stubFor(get(urlEqualTo("/v1/movie-info/" + id))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieInfo.json")
                )
        );

        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("reviews.json")
                )
        );

        this.webClient
                .get()
                .uri("/v1/movie/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                    assertEquals("Excellent Movie", movie.getReviewList().get(1).getComment());
                });
    }

    @Test
    void retrieveMovieByIdMovieInfoService404() {

        String id = "14";

        stubFor(get(urlEqualTo("/v1/movie-info/" + id))
                .willReturn(
                        aResponse()
                                .withStatus(404)
                )
        );

        this.webClient
                .get()
                .uri("/v1/movie/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("There is not MovieInfo for id 14");

    }

    @Test
    void retrieveMovieByIdMovieInfoService4XX() {

        String id = "14";

        stubFor(get(urlEqualTo("/v1/movie-info/" + id))
                .willReturn(
                        aResponse()
                                .withStatus(400)
                                .withBody("Error Bad Request")
                )
        );

        this.webClient
                .get()
                .uri("/v1/movie/{id}", id)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Error Bad Request");

    }

    @Test
    void retrieveMovieByIdReviewService404() {

        String id = "14";

        stubFor(get(urlEqualTo("/v1/movie-info/" + id))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBodyFile("movieInfo.json")
                )
        );

        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(
                        aResponse()
                                .withStatus(404)
                )
        );

        this.webClient
                .get()
                .uri("/v1/movie/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().isEmpty();
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                    assertEquals(0, movie.getReviewList().size());
                });

    }

    @Test
    void retrieveMovieByIdMovieInfoService500() {

        String id = "14";

        stubFor(get(urlEqualTo("/v1/movie-info/" + id))
                .willReturn(
                        aResponse()
                                .withStatus(500)
                                .withBody("Error in Server")
                )
        );

        this.webClient
                .get()
                .uri("/v1/movie/{id}", id)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MovieInfoService -> Error in Server");
    }

}
