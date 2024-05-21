package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.MovieReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class ReviewHandler {

    private final MovieReviewRepository movieReviewRepository;

    public ReviewHandler(MovieReviewRepository movieReviewRepository) {
        this.movieReviewRepository = movieReviewRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request
                .bodyToMono(Review.class)
                .flatMap(this.movieReviewRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getAllReviews(ServerRequest request) {
        Flux<Review> reviewByMovieInfoId;
        Optional<String> movieInfoId = request.queryParam("movieInfoId");

        if (movieInfoId.isPresent()) {
            reviewByMovieInfoId = this.movieReviewRepository.findReviewByMovieInfoId(Long.valueOf(movieInfoId.get()));
        } else {
            reviewByMovieInfoId = this.movieReviewRepository.findAll();
        }

        return ServerResponse.ok().body(reviewByMovieInfoId, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        String id = request.pathVariable("id");
        return this.movieReviewRepository.findById(id)
                .flatMap(existingReview -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            existingReview.setComment(reqReview.getComment());
                            existingReview.setRating(reqReview.getRating());
                            return existingReview;
                        })
                        .flatMap(this.movieReviewRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        String id = request.pathVariable("id");
        return this.movieReviewRepository.findById(id)
                .flatMap(existingReview -> movieReviewRepository.deleteById(id))
                .then(ServerResponse.noContent().build());

    }
}
