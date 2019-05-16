package contracts.api

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	description("""
Returns the details of match by id : 2

```
given:
	match-id
when:
	match details are requested
then:
	we'll return the match details
```

""")

	request {
		method GET()
		url "/match/2"
		headers {
			contentType applicationJson()
		}
	}

	response {
		status OK()
		headers {
			contentType applicationJson()
		}
		body("""
			{
				"match-id": 2,
				"name": "Barcelona - Real Madrid",
				"start-date": "2019-05-01T19:00:00",
				"status": "COMPLETED",
				"score": "1 - 1",
				"events": [
					{
						"minute": 1, 
						"type": "GOAL",
						"team": "Barcelona",
						"player-name": "Lionel Messi"
					},
					{
						"minute": 45, 
						"type": "RED",
						"team": "Real Madrid",						
						"player-name": "Sergio Ramos"
					},
					{
						"minute": 75, 
						"type": "GOAL",
						"team": "Real Madrid",						
						"player-name": "Luka Modric"
					}
				]
			}
		"""
		)
	}
}