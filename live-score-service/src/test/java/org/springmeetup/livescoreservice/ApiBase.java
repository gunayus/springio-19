package org.springmeetup.livescoreservice;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springmeetup.livescoreservice.kafka.KafkaService;
import org.springmeetup.livescoreservice.model.Match;
import org.springmeetup.livescoreservice.model.MatchEvent;
import org.springmeetup.livescoreservice.service.ApiRestService;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "server.port=0")
public class ApiBase {

	@LocalServerPort
	int port;

	@MockBean
	ApiRestService apiRestService;

	@MockBean
	KafkaService kafkaService;

	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost:" + this.port;

		Mockito.when(apiRestService.findMatchById(1l)).thenReturn(
				Mono.just(Match.builder()
					.matchId(1l)
					.name("Fenerbahçe - Galatasaray")
					.startDate(LocalDateTime.of(2019, 5, 16, 19, 0))
					.status("NOT_STARTED")
					.build()
				));

		Mockito.when(apiRestService.findMatchById(2l)).thenReturn(
				Mono.just(Match.builder()
					.matchId(2l)
					.name("Barcelona - Real Madrid")
					.startDate(LocalDateTime.of(2019, 5, 1, 19, 0))
					.status("COMPLETED")
					.score("1 - 1")
					.events(Arrays.asList(
							MatchEvent.builder().minute(1).type("GOAL").team("Barcelona").playerName("Lionel Messi").build(),
							MatchEvent.builder().minute(45).type("RED").team("Real Madrid").playerName("Sergio Ramos").build(),
							MatchEvent.builder().minute(75).type("GOAL").team("Real Madrid").playerName("Luka Modric").build()
					))
					.build()
				));

		Mockito.when(apiRestService.saveMatchDetails(Mockito.any())).thenReturn(Mono.just("OK"));
	}

}
