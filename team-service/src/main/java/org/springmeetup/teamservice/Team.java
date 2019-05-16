package org.springmeetup.teamservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {

	private String name;
	private String coach;
	private String city;
	private String stadium;
	private Integer establishedYear;

}
