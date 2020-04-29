package com.thepracticaldeveloper.reactiveweb.controller;

import com.thepracticaldeveloper.reactiveweb.domain.Quote;
import com.thepracticaldeveloper.reactiveweb.repository.QuoteMongoReactiveRepository;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import reactor.core.publisher.Mono;

@RestController
public class QuoteReactiveController {

    private static final int DELAY_PER_ITEM_MS = 100;

    private QuoteMongoReactiveRepository quoteMongoReactiveRepository;

    public QuoteReactiveController(final QuoteMongoReactiveRepository quoteMongoReactiveRepository) {
        this.quoteMongoReactiveRepository = quoteMongoReactiveRepository;
    }

    @GetMapping("/quotes-reactive")
    public Flux<Quote> getQuoteFlux() {
        // If you want to use a shorter version of the Flux, use take(100) at the end of the statement below
        return quoteMongoReactiveRepository.findAll().delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));
    }

    @GetMapping("/quotes-reactive-paged")
    public Flux<Quote> getQuoteFlux(final @RequestParam(name = "page") int page,
                                    final @RequestParam(name = "size") int size) {
        return quoteMongoReactiveRepository.retrieveAllQuotesPaged(PageRequest.of(page, size))
                .delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));
    }


    @DeleteMapping("/delete-quote-reactive/{quoteId}")
    public Mono deleteQuoteMono(@PathVariable("quoteId") String quoteId){
        final Mono<Quote> qouteToBeDeleted = quoteMongoReactiveRepository.findById(quoteId);
        if (Objects.isNull(qouteToBeDeleted)) {
            return Mono.empty();
        }
        return quoteMongoReactiveRepository.findById(quoteId)
            .switchIfEmpty(Mono.empty()).filter(Objects::nonNull).flatMap(quote -> quoteMongoReactiveRepository
            .delete(quote).then(Mono.just(quote)));
    }
}
