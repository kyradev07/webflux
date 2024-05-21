package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.MovieReviewRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

    @MockBean
    private MovieReviewRepository reviewRepository;

    @Autowired
    private WebTestClient webClient;

    private final String REVIEWS_URL = "http://localhost:8080/v1/review";

    @Test
    void addReview() {
        Review newReview = new Review("rev", 1L, "New Movie Review Added", 9.0);

        when(reviewRepository.save(isA(Review.class))).thenReturn(Mono.just(newReview));

        webClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(newReview)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review responseBody = reviewEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertNotNull(responseBody.getMovieInfoId());
                    assertEquals("New Movie Review Added", responseBody.getComment());
                    assertEquals("rev", responseBody.getReviewId());
                });
    }

    @Test
    void getAllReviews() {
        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0),
                new Review("rev", 3L, "Great Action Movie", 8.5),
                new Review(null, 2L, "Excellent Movie", 8.0));

        when(reviewRepository.findAll()).thenReturn(Flux.fromIterable(reviews));

        webClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(4);
    }

    @Test
    void updateReview() {
        String id = "rev";

        Review review = new Review("rev", 3L, "Great Action Movie", 8.5);
        Review reviewExisting = new Review("rev", 3L, "Normal Movie", 5.0);

        when(reviewRepository.findById(isA(String.class))).thenReturn(Mono.just(reviewExisting));
        when(reviewRepository.save(isA(Review.class))).thenReturn(Mono.just(review));

        webClient
                .put()
                .uri(REVIEWS_URL + "/{id}", id)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review responseBody = reviewEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertEquals("rev", responseBody.getReviewId());
                    assertEquals("Great Action Movie", responseBody.getComment());
                    assertEquals(8.5, responseBody.getRating());
                    assertEquals(3L, responseBody.getMovieInfoId());
                });
    }

    @Test
    void deleteReview() {
        String id = "rev";
        Review reviewExisting = new Review("rev", 3L, "Normal Movie", 5.0);

        when(reviewRepository.findById(isA(String.class))).thenReturn(Mono.just(reviewExisting));
        when(reviewRepository.deleteById(isA(String.class))).thenReturn(Mono.empty());

        webClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectBody(Void.class)
                .consumeWith(voidResponse -> {
                    Void responseBody = voidResponse.getResponseBody();
                    assertNull(responseBody);
                });
    }

    @Test
    void getReviewByMovieInfoId() {
        Long movieInfoId = 1L;
        List<Review> reviews = List.of(
                new Review(null, 1L, "Awesome Movie", 9.0),
                new Review(null, 1L, "Awesome Movie1", 9.0));

        URI uri = UriComponentsBuilder
                .fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", movieInfoId)
                .buildAndExpand().toUri();

        when(reviewRepository.findReviewByMovieInfoId(isA(Long.class))).thenReturn(Flux.fromIterable(reviews));

        webClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Review.class)
                .hasSize(2);

    }
}
