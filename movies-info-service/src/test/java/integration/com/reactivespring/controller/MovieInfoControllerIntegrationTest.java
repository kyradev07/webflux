package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
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
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MovieInfoControllerIntegrationTest {

    private static final String V_1_MOVIE_INFO = "/v1/movie-info";

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        List<MovieInfo> moviesInfo = List.of(
                new MovieInfo(null, "Batman Begins", 2005, List.of("Christian Bale", "Michael Caine"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises", 2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(moviesInfo).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {

        MovieInfo movieInfo = new MovieInfo(null, "Batman Begins1", 2005, List.of("Christian Bale", "Michael Caine"), LocalDate.parse("2005-06-15"));

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

                });
    }

    @Test
    void getAllMovieInfo() {
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
    void getAllMovieInfoByYear() {
        Integer year = 2008;

        URI uri = UriComponentsBuilder
                .fromUriString(V_1_MOVIE_INFO)
                .queryParam("year", year)
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);
    }

    @Test
    void getAllMovieInfoByName() {
        String name = "Dark Knight Rises";

        URI uri = UriComponentsBuilder
                .fromUriString(V_1_MOVIE_INFO + "/by")
                .queryParam("name", name)
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertEquals("abc", responseBody.getMovieInfoId());
                    assertEquals("Tom Hardy", responseBody.getCast().get(1));
                });
    }

    @Test
    void getAllMovieInfoByNameEmpty() {
        String name = "Dark Knight Rises On July";

        URI uri = UriComponentsBuilder
                .fromUriString(V_1_MOVIE_INFO + "/by")
                .queryParam("name", name)
                .buildAndExpand().toUri();

        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(Void.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    Void responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assertNull(responseBody);
                });
    }

    @Test
    void getMovieInfoById() {

        String id = "abc";

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
    void getMovieInfoByIdApproach2() {

        String id = "abc";

        webTestClient
                .get()
                .uri(V_1_MOVIE_INFO + "/{id}", id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.movieInfoId").isEqualTo(id)
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void getMovieInfoByIdNotFound() {

        String id = "abcd";

        webTestClient
                .get()
                .uri(V_1_MOVIE_INFO + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(Void.class)
                .consumeWith(responseMovie -> {
                    Void responseBody = responseMovie.getResponseBody();
                    assertNull(responseBody);
                });
    }

    @Test
    void updateMovieInfo() {

        String id = "abc";

        MovieInfo updatedMovieInfo = new MovieInfo(null, "Barbie", 2028, List.of("Ken", "Barbie"), LocalDate.parse("2012-07-20"));

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
    void updateMovieInfoNotFoundId() {

        String id = "abcd";

        MovieInfo updatedMovieInfo = new MovieInfo(null, "Barbie", 2028, List.of("Ken", "Barbie"), LocalDate.parse("2012-07-20"));

        webTestClient
                .put()
                .uri(V_1_MOVIE_INFO + "/{id}", id)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(Void.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Void responseBody = movieEntityExchangeResult.getResponseBody();
                    assertNull(responseBody);
                });
    }

    @Test
    void deleteMovieInfo() {

        String id = "abc";

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
}