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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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

    String REVIEWS_URL = "http://localhost:8080/v1/review";

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review("rev", 3L, "Great Action Movie", 8.5),
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

    @Test
    void getAllReviews() {
        webClient.get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(4);
    }

    @Test
    void getAllReviewsByMovieInfoId() {
        Long movieInfoId = 1L;

        URI uri = UriComponentsBuilder
                .fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", movieInfoId)
                .buildAndExpand().toUri();

        webClient.get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void updateReview() {
        String id = "rev";

        Review newReview = new Review(null, null, "Super Great Action-Terror Movie", 9.0);

        webClient
                .put()
                .uri(REVIEWS_URL + "/{id}", id)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review responseBody = reviewEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertEquals("rev", responseBody.getReviewId());
                    assertEquals("Super Great Action-Terror Movie", responseBody.getComment());
                    assertEquals(9.0, responseBody.getRating());
                });
    }

    @Test
    void deleteReview() {
        String id = "rev";

        webClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void notFoundReview() {
        String id = "rev4";

        webClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .isEqualTo("Review not found for the ID -> rev4");
    }
}
