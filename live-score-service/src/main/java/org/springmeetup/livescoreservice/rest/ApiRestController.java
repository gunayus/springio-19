package org.springmeetup.livescoreservice.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springmeetup.livescoreservice.kafka.KafkaService;
import org.springmeetup.livescoreservice.service.ApiRestService;
import org.springmeetup.livescoreservice.model.Match;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ApiRestController {

	private final ApiRestService apiRestService;
	private final KafkaService kafkaService;


	@GetMapping("/match/{id}")
	public Mono<Match> getMatchById(@PathVariable("id") Long id) {
		return apiRestService.findMatchById(id);
	}

	@PostMapping("/match")
	public Mono<String> saveMatchDetails(@RequestBody Match match) {
		return apiRestService.saveMatchDetails(match);
	}

	@GetMapping(value = "/match/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<Match>> matchStream(@PathVariable("id") Long id) {
		return kafkaService.getEventPublisher()
				.doOnNext(stringServerSentEvent -> log.info(stringServerSentEvent.data()))
				.map(stringServerSentEvent -> {
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategy.KebabCaseStrategy());

					Match match = null;
					try {
						match = objectMapper.readValue(stringServerSentEvent.data(), Match.class);
					} catch (Exception ex) {
						return null;
					}

					return ServerSentEvent.builder(match).build();
				})
				.filter(matchServerSentEvent -> matchServerSentEvent != null)
				.filter(matchServerSentEvent -> matchServerSentEvent.data().getMatchId().equals(id))
				.onErrorContinue((throwable, o) -> log.error(throwable.getMessage()));
	}
}
