package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MovieInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    private static final String V_1_MOVIE_INFO = "/v1/movie-info";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoService;

    @Test
    void getAllMoviesInfo() {
        List<MovieInfo> moviesInfo = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Caine"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(movieInfoService.getAllMovieInfos()).thenReturn(Flux.fromIterable(moviesInfo));

        webTestClient
                .get()
                .uri(V_1_MOVIE_INFO)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {

        String id = "abc";
        MovieInfo movieInfo = new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoService.getMovieInfoById(isA(String.class))).thenReturn(Mono.just(movieInfo));


        webTestClient
                .get()
                .uri(V_1_MOVIE_INFO + "/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(responseMovie -> {
                    MovieInfo responseBody = responseMovie.getResponseBody();
                    assert responseBody != null;
                    assertEquals("abc", responseBody.getMovieInfoId());
                });
    }

    @Test
    void addMovieInfo() {

        MovieInfo movieInfo = new MovieInfo("mockId", "Batman Begins1", 2005, List.of("Christian Bale", "Michael Caine"), LocalDate.parse("2005-06-15"));

        when(movieInfoService.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

        webTestClient
                .post()
                .uri(V_1_MOVIE_INFO)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertNotNull(responseBody.getMovieInfoId());
                    assertEquals("mockId", responseBody.getMovieInfoId());
                });
    }

    @Test
    void updateMovieInfo() {

        String id = "abc";

        MovieInfo updatedMovieInfo = new MovieInfo("abc", "Barbie", 2028, List.of("Ken", "Barbie"), LocalDate.parse("2012-07-20"));

        when(movieInfoService.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(updatedMovieInfo));

        webTestClient
                .put()
                .uri(V_1_MOVIE_INFO + "/{id}", id)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieEntityExchangeResult -> {
                    MovieInfo responseBody = movieEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertEquals("abc", responseBody.getMovieInfoId());
                    assertEquals("Barbie", responseBody.getName());
                    assertEquals("Ken", responseBody.getCast().get(0));
                    assertEquals(2028, responseBody.getYear());
                    assertEquals(LocalDate.of(2012, 7, 20), responseBody.getReleaseDate());
                });
    }

    @Test
    void deleteMovieInfo() {

        String id = "abc";

        when(movieInfoService.deleteMovieInfo(isA(String.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(V_1_MOVIE_INFO + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent() // Se podría dejar hasta acá el test
                .expectBody(Void.class)
                .consumeWith(response -> {
                    Void responseBody = response.getResponseBody();
                    assertNull(responseBody);
                });
    }

    @Test
    void requestBodyValidations() {

        MovieInfo movieInfo = new MovieInfo("mockId", null, -5021, List.of(""), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(V_1_MOVIE_INFO)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    assertEquals("Movie cast must be present, Movie name must be present, Movie year must be positive value", responseBody);
                });
    }

}
