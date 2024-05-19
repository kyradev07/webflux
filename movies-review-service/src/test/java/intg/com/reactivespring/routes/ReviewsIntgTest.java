package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.MovieReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class ReviewsIntgTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    MovieReviewRepository movieReviewRepository;

    String REVIEWS_URL = "http://localhost:8080//v1/review";

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));

        movieReviewRepository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieReviewRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        Review newReview = new Review(null, 1L, "New Movie Review Added", 9.0);

        webClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    Review responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertNotNull(responseBody.getMovieInfoId());
                    assertEquals("New Movie Review Added", responseBody.getComment());

                });
    }
}
