package contracts.api

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	description("""
Posts the details of a match 

```
given:
	match-details
when:
	match details are posted
then:
	we'll return a simple notification of operations result OK / NOK
```

""")

	request {
		method POST()
		url "/match"
		headers {
			contentType applicationJson()
		}
		body("""
			{
				"match-id": 3,
				"name": "Liverpool - Arsenal",
				"start-date": "2019-05-01T19:00:00",
				"status": "COMPLETED",
				"score": "0 - 0",
				"events": [
					{
						"minute": 1, 
						"type": "YELLOW",
						"team": "Liverpool",						
						"player-name": "Sadio Mané"
					},
					{
						"minute": 90, 
						"type": "RED",
						"team": "Arsenal",						
						"player-name": "Aaron Ramsey"
					}
				]
			}
		"""
		)
	}

	response {
		status OK()
		headers {
			contentType("text/plain;charset=UTF-8")
		}
		body(
			"OK"
		)

	}
}