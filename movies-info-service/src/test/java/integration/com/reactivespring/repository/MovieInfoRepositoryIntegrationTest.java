package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIntegrationTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

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
    void findAll() {
        Flux<MovieInfo> allMovieInfo = movieInfoRepository.findAll().log();

        StepVerifier.create(allMovieInfo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findById() {
        Mono<MovieInfo> movieInfo = movieInfoRepository.findById("abc").log();

        StepVerifier.create(movieInfo)
                .assertNext(movie -> {
                    assertEquals("Dark Knight Rises", movie.getName());
                    assertEquals(2012, movie.getYear());
                    assertEquals("abc", movie.getMovieInfoId());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        MovieInfo movie = new MovieInfo(null, "Oppenheimer", 2023, List.of("Cillian Murphy", "Florence Pugh"), LocalDate.parse("2023-07-21"));

        Mono<MovieInfo> movieInfo = movieInfoRepository.save(movie).log();

        StepVerifier.create(movieInfo)
                .assertNext(newMovie -> {
                    assertNotNull(newMovie.getMovieInfoId());
                    assertEquals("Oppenheimer", newMovie.getName());
                    assertEquals("Florence Pugh", newMovie.getCast().get(1));
                    assertEquals(2023, newMovie.getYear());
                })
                .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        MovieInfo oldMovie = movieInfoRepository.findById("abc").block();

        assert oldMovie != null;
        oldMovie.setYear(2012);
        oldMovie.setCast(List.of("Christian Bale", "Michael Caine", "Anne Hathaway"));

        Mono<MovieInfo> updateMovie = movieInfoRepository.save(oldMovie).log();

        StepVerifier.create(updateMovie)
                .assertNext(updatedMovie -> {
                    assertNotNull(updatedMovie.getMovieInfoId());
                    assertEquals("Dark Knight Rises", updatedMovie.getName());
                    assertEquals("Anne Hathaway", updatedMovie.getCast().get(2));
                    assertEquals(2012, updatedMovie.getYear());
                })
                .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        movieInfoRepository.deleteById("abc").block();

        Flux<MovieInfo> all = movieInfoRepository.findAll().log();

        StepVerifier.create(all)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByYear() {
        Flux<MovieInfo> movieInfo = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(movieInfo)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByName() {
        Mono<MovieInfo> movieInfo = movieInfoRepository.findByName("The Dark Knight").log();

        StepVerifier.create(movieInfo)
                .assertNext(movie -> {
                    assertEquals("The Dark Knight", movie.getName());
                    assertEquals(2008, movie.getYear());
                    assertEquals("HeathLedger", movie.getCast().get(1));
                })
                .verifyComplete();
    }


}