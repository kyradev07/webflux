package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

public class SinkTest {

    //Replay, envía TODOS los valores emitidos desde el inicio, a cualquier subscriptor cuando se subscriban.
    @Test
    void sinkReplay() {
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);


        replaySink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #1 : " + i));

        replaySink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #2 : " + i));


        replaySink.tryEmitNext(4);
        replaySink.tryEmitNext(5);

        replaySink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #3 : " + i));
    }

    //Multicast: envía los valores emitidos después de la subscripción. Si se subscriben después del último valor emitido, no enviará ninguno.
    @Test
    void sinkMulticast() {
        Sinks.Many<Integer> multicastSink = Sinks.many().multicast().onBackpressureBuffer();

        multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        multicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);


        multicastSink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #1 : " + i));

        multicastSink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #2 : " + i));


        multicastSink.tryEmitNext(4);
        multicastSink.tryEmitNext(5);

        multicastSink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #3 : " + i));
    }

    //Unicast: solo permite un único subscritor
    @Test
    void sinkUnicast() {
        Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();

        unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        unicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);


        unicastSink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #1 : " + i));

        unicastSink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #2 : " + i));


        unicastSink.tryEmitNext(4);
        unicastSink.tryEmitNext(5);

        unicastSink.asFlux()
                .subscribe(i -> System.out.println("Subscriber #3 : " + i));
    }
}
