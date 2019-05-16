package contracts.api

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	description("""
Returns the details of match by id : 1

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
		url "/match/1"
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
				"match-id": 1,
				"name": "Fenerbahçe - Galatasaray",
				"start-date": "2019-05-16T19:00:00",
				"status": "NOT_STARTED"
			}
		"""
		)
	}
}
