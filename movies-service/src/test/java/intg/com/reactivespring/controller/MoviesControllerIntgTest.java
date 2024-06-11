package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
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
    WebTestClient webTestClient;

    @Test
    void retrieveMovieById() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-info" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieInfo.json")));

        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        //when
        webTestClient
                .get()
                .uri("/v1/movie/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }

    @Test
    void retrieveMovieById_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-info" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)
                ));

        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        //when
        webTestClient
                .get()
                .uri("/v1/movie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is not MovieInfo for id " + movieId);

        WireMock.verify(3, getRequestedFor(urlEqualTo("/v1/movie-info" + "/" + movieId)));

    }

    @Test
    void retrieveMovieById_reviews_404() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-info" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieInfo.json")
                ));

        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(aResponse()
                        .withStatus(404)));

        //when
        webTestClient
                .get()
                .uri("/v1/movie/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().isEmpty();
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });;

    }

    @Test
    void retrieveMovieById_5XX() {
        //given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-info" + "/" + movieId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")

                ));

        //when
        webTestClient
                .get()
                .uri("/v1/movie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in MovieInfoService -> MovieInfo Service Unavailable");

        WireMock.verify(7, getRequestedFor(urlEqualTo("/v1/movie-info" + "/" + movieId)));

    }

    @Test
    void retrieveMovieById_Reviews_5XX() {
        //given
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movie-info" + "/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieInfo.json")
                ));



        stubFor(get(urlPathEqualTo("/v1/review"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review Service Not Available Test")));

        //when
        webTestClient
                .get()
                .uri("/v1/movie/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server Exception in ReviewsService Review Service Not Available Test");

        WireMock.verify(6, getRequestedFor(urlPathMatching("/v1/review*")));

    }

}
