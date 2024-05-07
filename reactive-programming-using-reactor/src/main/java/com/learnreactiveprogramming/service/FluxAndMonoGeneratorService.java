package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Mono<String> nameMono() {
        return Mono.just("Eliza").log();
    }

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("John", "Ane", "Chloe"))
                .log();
    }

    public Flux<String> namesMap() {
        return Flux.fromIterable(List.of("John", "Ane", "Chloe"))
                .map(String::toUpperCase)
                .log();
    }

    public Flux<String> namesImmutability() {
        Flux<String> namesFlux = Flux.fromIterable(List.of("John", "Ane", "Chloe"));
        namesFlux.map(String::toUpperCase);
        return namesFlux;
    }

    public Flux<String> namesFilter(int lengthName) {
        return Flux.fromIterable(List.of("John", "Ane", "Chloe"))
                .filter(name -> name.length() > lengthName)
                .map(name -> name.length() + "-" + name)
                .log();
    }

    //Transform one source element to a Flux of 1 to N elements.
    public Flux<String> namesFlatMap() {
        return Flux.fromIterable(List.of("ANABELLE"))
                .flatMap(name -> Flux.fromArray(name.split("")))
                .log();
    }

    //Async way in flatMap. Do not conserve the order of the flow
    public Flux<String> namesAsyncFlatMap() {
        return Flux.fromIterable(List.of("ANABELLE", "ELIZA", "DAN"))
                .flatMap(this::splitStringDelay)
                .log();
    }

    //Async way in concatMap. Conserve the order of the flow
    public Flux<String> namesAsyncConcatMap() {
        return Flux.fromIterable(List.of("ANABELLE", "ELIZA", "DAN"))
                .concatMap(this::splitStringDelay)
                .log();
    }

    public Mono<List<String>> nameFlatMap() {
        return Mono.just("ELIZA")
                .flatMap(name -> Mono.just(List.of(name.split(""))))
                .log();
    }

    //Transform from Mono to Flux
    public Flux<String> nameFlatMapMany() {
        return Mono.just("ELIZA-FLAT")
                .flatMapMany(this::splitStringDelay)
                .log();
    }

    //Transform method change from one type to another, Flux to Mono or vice. Receive a method of type Function
    //Allow to reuse methods
    public Flux<String> nameFluxTransformAndDefaultValueIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> filterAndMap = name -> name
                .filter(nameLength -> nameLength.length() > stringLength)
                .map(String::toUpperCase);

        return Flux.fromIterable(List.of("kyra", "luz", "isa", "ismael"))
                .transform(filterAndMap)
                .flatMap(name -> Flux.fromArray(name.split("")))
                .defaultIfEmpty("Empty Stream, Default Value")
                .log();
    }

    public Flux<String> nameFluxTransformSwitch(int stringLength) {
        Function<Flux<String>, Flux<String>> filterAndMap = name -> name
                .filter(nameLength -> nameLength.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(nameSplit -> Flux.fromArray(nameSplit.split("")));

        return Flux.fromIterable(List.of("kyra", "luz", "isa", "ismael"))
                .transform(filterAndMap)
                .switchIfEmpty(Flux.just("empty stream"))
                .transform(filterAndMap)
                .log();
    }

    public Flux<String> nameConcatFlux() {
        Flux<String> firstStream = Flux.just("A", "B", "C");
        Flux<String> secondStream = Flux.just("D", "E", "F").delayElements(Duration.ofSeconds(1));
        Flux<String> thirdStream = Flux.just("G", "H", "I");

        return Flux.concat(firstStream, secondStream, thirdStream).log();
    }

    public Flux<String> nameConcatWith() {
        Flux<String> firstStream = Flux.just("A", "B", "C");
        Flux<String> secondStream = Flux.just("D", "E", "F");

        return firstStream.concatWith(secondStream).log();
    }

    public Flux<String> nameConcatWithMono() {
        Mono<String> a = Mono.just("A");
        Mono<String> b = Mono.just("B");
        Mono<String> c = Mono.just("C");
        Mono<String> z = Mono.just("Z");

        return a.concatWith(b).concatWith(c).concatWith(z).log();
    }

    public Flux<String> nameMerge() {
        Flux<String> firstStream = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(1500));
        Flux<String> secondStream = Flux.just("D", "E", "F").delayElements(Duration.ofSeconds(1));
        Flux<String> thirdStream = Flux.just("G", "H", "I");
        return Flux.merge(firstStream, secondStream, thirdStream).log();
    }

    public Flux<String> nameMergeWith() {
        Flux<String> firstStream = Flux.just("A", "B", "C");
        Flux<String> secondStream = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(1500));
        Flux<String> thirdStream = Flux.just("G", "H", "I").delayElements(Duration.ofMillis(1000));
        return firstStream.mergeWith(secondStream).mergeWith(thirdStream).log();
    }

    public Flux<String> nameMergeWithMono() {
        Mono<String> a = Mono.just("A");
        Mono<String> b = Mono.just("B").delayElement(Duration.ofMillis(1500));;
        Mono<String> c = Mono.just("C").delayElement(Duration.ofMillis(1000));;
        Mono<String> z = Mono.just("Z");

        return z.mergeWith(b.mergeWith(c)).mergeWith(a).log();
    }

    public Flux<String> nameMergeSequential() {
        Flux<String> secondStream = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(1500));
        Flux<String> firstStream = Flux.just("A", "B", "C");
        Flux<String> thirdStream = Flux.just("G", "H", "I").delayElements(Duration.ofMillis(1000));
        return Flux.mergeSequential(secondStream, firstStream, thirdStream).log();
    }

    public Flux<String> nameZipFlux() {
        Flux<String> firstStream = Flux.just("A", "B", "C");
        Flux<String> secondStream = Flux.just("D", "E", "F");

        return Flux.zip(secondStream, firstStream, (s, f) -> f + s)
                .log();
    }

    public Flux<String> nameZipTuple() {
        Flux<String> firstStream = Flux.just("A", "B", "C");
        Flux<String> secondStream = Flux.just("D", "E", "F");
        Flux<String> thirdStream = Flux.just("1", "2", "3").delayElements(Duration.ofMillis(5000));
        Flux<String> fourthStream = Flux.just("4", "5", "9");

        return Flux.zip(fourthStream, secondStream, firstStream, thirdStream)
                .map(tuple -> tuple.getT1() + "-" + tuple.getT2() + "-" + tuple.getT3() + "-" + tuple.getT4())
                .log();
    }

    public Flux<String> nameZipWith() {
        Flux<String> firstStream = Flux.just("A", "B", "C");
        Flux<String> secondStream = Flux.just("D", "E", "F");

        return secondStream.zipWith(firstStream, (s, f) -> s + f).log();
    }

    public Mono<String> nameZipWithMono() {
        Mono<String> a = Mono.just("A");
        Mono<String> z = Mono.just("Z");

        return z.zipWith(a)
                .map(tuple -> tuple.getT1() + tuple.getT2())
                .log();
    }

    private Flux<String> splitStringDelay(String name) {
        String[] parts = name.split("");
        int delay = new Random().nextInt(1000);
        return Flux.fromArray(parts).delayElements(Duration.ofMillis(delay));
    }

    public static void main(String[] args) {

        FluxAndMonoGeneratorService fluxMonoGenerator = new FluxAndMonoGeneratorService();

        /*//Print all names in Flux. Flux is used to 0 - n elements
        fluxMonoGenerator.namesFlux().subscribe(name -> System.out.printf("Flux name -> %s\n", name));

        //Print the name in Mono. Mono is used to 0 - 1 element
        fluxMonoGenerator.nameMono().subscribe(name -> System.out.printf("Mono name -> %s\n", name));

        //Transform names to UpperCase. Transform the data. One to One transformation.
        //Synchronous. Do NOT support transformation that return Publisher
        fluxMonoGenerator.namesMap().subscribe(name -> System.out.printf("Map name -> %s\n", name));

        //Immutability of data in reactive streams. Do not mutate the old date, always return a new stream
        fluxMonoGenerator.namesImmutability().subscribe(name -> System.out.printf("Immutability name -> %s\n", name));

        //Filter names whose length is greater than 3
        fluxMonoGenerator.namesFilter(3).subscribe(name -> System.out.printf("Filter name -> %s\n", name));

        //FlatMap. One to N transformation. Asynchronous transformation. Support transformation that return Publisher
        fluxMonoGenerator.namesFlatMap().subscribe(name -> System.out.printf("FlatMap name -> %s", name));

        //Async FlatMap. Do not preserve the ordering sequence of the reactive streams
        fluxMonoGenerator.namesAsyncFlatMap().subscribe(name -> System.out.printf("AsyncFlatMap name -> %s\n", name));

        //ConcatMap. Same as FlatMap, but preserves the ordering sequence of the reactive streams
        fluxMonoGenerator.namesAsyncConcatMap().subscribe(name -> System.out.printf("AsyncConcatMap name -> %s\n", name));

        //FlatMap in Mono
        fluxMonoGenerator.nameFlatMap().subscribe(name -> System.out.printf("FlatMap name -> %s\n", name));

        //FlatMapMany transform from Mono to Flux
        fluxMonoGenerator.nameFlatMapMany().subscribe(name -> System.out.printf("FlatMapMany name -> %s\n", name));

        //Transform method allow to reuse a common functionality using Function Interface
        fluxMonoGenerator.nameFluxTransformAndDefaultValueIfEmpty(3).subscribe(name -> System.out.printf("FluxTransform name -> %s\n", name));

        //defaultIfEmpty accepts the same value we are working with, in this case, String
        fluxMonoGenerator.nameFluxTransformAndDefaultValueIfEmpty(10).subscribe(name -> System.out.printf("DefaultIFEmpty name -> %s\n", name));

        //switchIfEmpty accepts other Publisher(Flux or Mono). It's mean, change to other subscription
        fluxMonoGenerator.nameFluxTransformSwitch(10).subscribe(name -> System.out.printf("switchIfEmpty name -> %s\n", name));

        //concat is a static method, allow to combine different reactive stream, It's sequential, wait until the first one end to start with the other.
        fluxMonoGenerator.nameConcatFlux().subscribe(name -> System.out.printf("Concat name -> %s\n", name));

        //concatWith allow to combine different reactive stream from the same publisher, It's sequential, wait until the first one end to start with the other.
        fluxMonoGenerator.nameConcatWith().subscribe(name -> System.out.printf("ConcatWith name -> %s\n", name));

        //concatWith allow to combine different reactive stream from the same Mono Publisher and return a Flux,
        // It's sequential, wait until the first one end to start with the other.
        fluxMonoGenerator.nameConcatWithMono().subscribe(name -> System.out.printf("ConcatWithMono name -> %s\n", name));

        //merge is a static method, allow to combine n streams. It's not sequential.
        //It receives at the same time the values, depending on the delay or other situation
        fluxMonoGenerator.nameMerge().subscribe(name -> System.out.printf("Merge name -> %s\n", name));

        fluxMonoGenerator.nameMergeWith().subscribe(name -> System.out.printf("MergeWith name -> %s\n", name));

        fluxMonoGenerator.nameMergeWithMono().subscribe(name -> System.out.printf("MergeWithMono -> %s\n", name));

        //mergeSequential. Ignore the delay of each stream and process the elements in order like concatMap
        fluxMonoGenerator.nameMergeSequential().subscribe(name -> System.out.printf("MergeSequential -> %s\n", name));*/

        //zip is static method. Allow to combine the n values of all the streams into one
        //It works sequentially, ff some of the publisher has delay, it waits until resolves
        fluxMonoGenerator.nameZipFlux().subscribe(System.out::println);

        //Work until 8 publishers
        fluxMonoGenerator.nameZipTuple().subscribe(System.out::println);
    }
}
