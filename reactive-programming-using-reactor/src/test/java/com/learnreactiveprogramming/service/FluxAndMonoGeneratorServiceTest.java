package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService generator = new FluxAndMonoGeneratorService();

    @Test
    @DisplayName("Verify the total of elements and the complete method")
    void allNamesAndCompleteMethodFlux() {
        Flux<String> namesFlux = generator.namesFlux();

        StepVerifier.create(namesFlux)
                .expectNext("John", "Ane", "Chloe")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify the first name, the number of remains elements and the complete method")
    void firstNamesRemainsAndCompleteFlux() {
        Flux<String> namesFlux = generator.namesFlux();

        StepVerifier.create(namesFlux)
                .expectNext("John")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("verify that transform the names to Upper Case")
    void namesMap() {
        Flux<String> namesFlux = generator.namesMap();

        StepVerifier.create(namesFlux)
                .expectNext("JOHN", "ANE", "CHLOE")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify the immutability data from reactive streams")
    void namesImmutability() {
        Flux<String> namesFlux = generator.namesImmutability();

        StepVerifier.create(namesFlux)
                .expectNext("John", "Ane", "Chloe")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that filter the names whose length is greater than the given number")
    void namesFilter() {
        Flux<String> namesFlux = generator.namesFilter(3);

        StepVerifier.create(namesFlux)
                .expectNext("4-John", "5-Chloe")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that a given name is been splitting in its individual letters")
    void namesFlatMap() {
        Flux<String> namesFlux = generator.namesFlatMap();

        StepVerifier.create(namesFlux)
                .expectNext("A", "N", "A", "B", "E", "L", "L", "E")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that a given name is been splitting in its individual letters with async behavior.")
    void namesFlatMapAsync() {
        //The letters are received in random order depending on the delay.
        Flux<String> namesFlux = generator.namesAsyncFlatMap();

        StepVerifier.create(namesFlux)
                .expectNextCount(16)
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that a given name is been splitting in its individual letters with async behavior.")
    void namesConcatMapAsync() {
        //The letters are received in order
        Flux<String> namesFlux = generator.namesAsyncConcatMap();

        StepVerifier.create(namesFlux)
                .expectNext("A", "N", "A", "B", "E", "L", "L", "E", "E", "L", "I", "Z", "A", "D", "A", "N")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that a given name is been splitting in its individual letters with async behavior.")
    void nameMonoFlatMap() {
        //The letters are received in order
        Mono<List<String>> namesFlux = generator.nameFlatMap();

        StepVerifier.create(namesFlux)
                .expectNext(List.of("E", "L", "I", "Z", "A"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that a given name is been splitting in its individual letters with async behavior.")
    void nameMonoFlatMapMany() {
        //The letters are received in order
        Flux<String> namesFlux = generator.nameFlatMapMany();

        StepVerifier.create(namesFlux)
                .expectNext("E", "L", "I", "Z", "A", "-", "F", "L", "A", "T")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that a given names are divided in its letters and filter by a length")
    void nameFluxTransform() {
        //The letters are received in order
        Flux<String> namesFlux = generator.nameFluxTransformAndDefaultValueIfEmpty(3);

        StepVerifier.create(namesFlux)
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    @DisplayName("Empty stream, defaultIfEmpty")
    void defaultIfEmptyStream() {
        //When a stream is empty, immediately calls onComplete method returning a Flux.empty()
        //Use defaultIsEmpty to return a default value.
        Flux<String> namesFluxEmpty = generator.nameFluxTransformAndDefaultValueIfEmpty(10);

        StepVerifier.create(namesFluxEmpty)
                .expectNext("Empty Stream, Default Value")
                .verifyComplete();
    }

    @Test
    @DisplayName("Empty stream, switchIfEmpty")
    void swtichIfEmptyStream() {
        //When a stream is empty, immediately calls onComplete method returning a Flux.empty()
        //Use defaultIsEmpty to return a default value.
        Flux<String> namesFluxEmpty = generator.nameFluxTransformSwitch(10);

        StepVerifier.create(namesFluxEmpty)
                .expectNext("E", "M", "P", "T", "Y", " ", "S", "T", "R", "E", "A", "M")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were concat with the static method")
    void concatStream() {
        Flux<String> namesFluxEmpty = generator.nameConcatFlux();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("A", "B", "C", "D", "E", "F", "G", "H", "I")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were concat")
    void concatWithStream() {
        Flux<String> namesFluxEmpty = generator.nameConcatWith();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were concat using Mono")
    void concatStreamMono() {
        Flux<String> namesFluxEmpty = generator.nameConcatWithMono();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("A", "B", "C", "Z")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were merge")
    void mergeStream() {
        Flux<String> namesFluxEmpty = generator.nameMerge();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("G", "H", "I", "D", "A", "E", "B", "F", "C")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were mergeWith")
    void mergeWithStream() {
        Flux<String> namesFluxEmpty = generator.nameMergeWith();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("A", "B", "C", "G", "D", "H", "E", "I", "F")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were mergeWith Mono")
    void mergeWithMonoStream() {
        Flux<String> namesFluxEmpty = generator.nameMergeWithMono();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("Z", "A", "C", "B")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were mergeWith Mono")
    void mergeSequentialStream() {
        Flux<String> namesFluxEmpty = generator.nameMergeSequential();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("D", "E", "F", "A", "B", "C", "G", "H", "I")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were zip")
    void zipStream() {
        Flux<String> namesFluxEmpty = generator.nameZipFlux();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were zip Tuple")
    void zipTupleStream() {
        Flux<String> namesFluxEmpty = generator.nameZipTuple();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("4-D-A-1", "5-E-B-2", "9-F-C-3")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were zipWith")
    void zipWithStream() {
        Flux<String> namesFluxEmpty = generator.nameZipWith();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("DA", "EB", "FC")
                .verifyComplete();
    }

    @Test
    @DisplayName("Verify that 2 stream were zipWith Mono")
    void zipWithMonoStream() {
        Mono<String> namesFluxEmpty = generator.nameZipWithMono();

        StepVerifier.create(namesFluxEmpty)
                .expectNext("ZA")
                .verifyComplete();
    }

}