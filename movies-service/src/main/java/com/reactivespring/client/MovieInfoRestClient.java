package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MovieInfoRestClient {

    private final WebClient webClient;

    @Value("${restClient.movieInfoUrl}")
    private String MOVIE_INFO_URL;

    public MovieInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        String url = MOVIE_INFO_URL + "/{id}";
        return this.webClient
                .get()
                .uri(url, movieId)
                .retrieve()
                .bodyToMono(MovieInfo.class)
                .log();
    }
}
