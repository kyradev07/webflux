package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("v1/movie")
public class MovieController {

    private final MovieInfoRestClient movieInfoRestClient;
    private final ReviewRestClient reviewRestClient;

    public MovieController(MovieInfoRestClient movieInfoRestClient, ReviewRestClient reviewRestClient) {
        this.movieInfoRestClient = movieInfoRestClient;
        this.reviewRestClient = reviewRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable String id) {
        return this.movieInfoRestClient.retrieveMovieInfo(id)
                .flatMap(movieInfo -> this.reviewRestClient.retrieveReviews(movieInfo.getMovieInfoId())
                        .collectList()
                        .map(reviewsList -> new Movie(movieInfo, reviewsList)));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MovieInfo> retrieveMovieInfoStream() {
        return this.movieInfoRestClient.retrieveMovieInfoStream();
    }
}
