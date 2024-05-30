package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    Mono<Throwable> error;
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        error = Mono.error(new MoviesInfoClientException("There is not MovieInfo for id " + movieId, clientResponse.statusCode().value()));
                    } else {
                        error = clientResponse
                                .bodyToMono(String.class)
                                .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(responseMessage, clientResponse.statusCode().value())));
                    }
                    return error;
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException("Server Exception in MovieInfoService -> " + responseMessage)))
                )
                .bodyToMono(MovieInfo.class)
                .log();
    }
}
