package org.springmeetup.teamservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TeamRestController {

	private final Map<String, Team> teamsMap = new HashMap<>();

	@PostConstruct
	public void initTeams() {
		teamsMap.put("Fenerbahçe", Team.builder()
				.name("Fenerbahçe")
				.coach("Ersun Yanal")
				.city("İstanbul")
				.stadium("Şükrü Saracoğlu")
				.establishedYear(1907)
				.build());
	}

	@GetMapping("/team/{name}")
	public Mono<Team> findTeamByName(@PathVariable("name") String name) {
		return Mono.just(teamsMap.get(name));
	}

}
