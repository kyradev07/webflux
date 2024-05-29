package com.reactivespring.client;

import com.reactivespring.domain.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
public class ReviewRestClient {

    private final WebClient webClient;

    @Value("${restClient.reviewUrl}")
    private String REVIEW_URL;

    public ReviewRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieInfoId) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(REVIEW_URL)
                .queryParam("movieInfoId", movieInfoId)
                .buildAndExpand()
                .toUriString();

        return this.webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Review.class)
                .log();
    }
}
