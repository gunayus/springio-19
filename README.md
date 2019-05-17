
# Cloud-native, Reactive, live score streaming app development workshop
This repository contains the source code and the resources for workshop from Spring IO 2019, Barcelona.

https://2019.springio.net/sessions/cloud-native-reactive-spring-boot-application-development-workshop


## Prerequisites
In order to follow the workshop, it's good idea to have the following prerequisites ready on your system
    

+ JDK 8 or above
+ IDE supporting Spring development, e.g. STS, Eclipse, Intellij IDEA, etc.
+ Redis
+ Kafka

### Installing Redis
Redis can be installed in two ways

1 - Build from source code

https://redis.io/topics/quickstart

As explained in Redis quick start installation section, Redis can be built from source code and installed. 

```
wget http://download.redis.io/redis-stable.tar.gz 
tar xvzf redis-stable.tar.gz
cd redis-stable
make
```

2 - Use docker image

Follow instructions listed in Redis official dockerhub page

https://hub.docker.com/_/redis

or you can use following docker-compose.yml content

```
version: '2'

services:
  redis:
    image: 'bitnami/redis:latest'
    environment:
      # ALLOW_EMPTY_PASSWORD is recommended only for development.
      - ALLOW_EMPTY_PASSWORD=yes
    labels:
      kompose.service.type: nodeport
    ports:
      - '6379:6379'
    volumes:
      - ~/volumes/redis:/var/lib/redis
    command: redis-server --requirepass password
```


### Installing Kafka
Download latest kafka release as explained in 

https://kafka.apache.org/quickstart

```
> tar -xzf kafka_2.12-2.2.0.tgz
> cd kafka_2.12-2.2.0
> bin/zookeeper-server-start.sh config/zookeeper.properties
> bin/kafka-server-start.sh config/server.properties
```

## Workshop

During the workshop, you can always refer to the modules in this repository. You can copy / paste some sections. But it's strongly recommended to try building yourself.

It's recommended to use Spring Starter (start.spring.io) from within your favourite IDE in order to keep it simple to create a new module with proper spring boot and spring cloud dependencies.

All of the modules listed in this repository have following maven definition. 

```
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.5.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>org.springmeetup</groupId>
	<artifactId>live-score-service</artifactId>
		
	<properties>
		<java.version>1.8</java.version>
		<spring-cloud.version>Greenwich.SR1</spring-cloud.version>
	</properties>
```

Please select following options:

+ Java version : 1.8
+ Spring Boot version : 2.1.5 (latest stable)
+ group Id : org.springmeetup
+ artifacet Id : name according to the reference module names (live-score-service, config-service, service-registry, gateway-service, etc.)


### config-service application

create a new module 'config-service' from Spring Initializer. 

```
	<groupId>org.springmeetup</groupId>
	<artifactId>config-service</artifactId>
```

in the dependencies section, select 'Config Server'. eventually following dependency should be in pom.xml.  

```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-config-server</artifactId>
		</dependency>
```

add '@EnableConfigServer' annotation to 'ConfigServiceApplication.java'. should look like this

```
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServiceApplication.class, args);
	}

}
```

edit 'application.properties' file so that server port and configuration repository is defined. 

```
spring.application.name=config-service

server.port=8888

spring.cloud.config.server.git.uri=https://github.com/gunayus/springio-19-config.git
```

you can always clone my config repository to your local environment and make modifications. you should not forget 'git add ' and 'git commit' commands. In that case git uri should point to the local folder

```
spring.cloud.config.server.git.uri=absolute-path/to/config-repo-folder/springio-19-config
```


run 'ConfigServiceApplication.java' as Java application from your IDE or following

```
cd config-service
mvn spring-boot:run
```

try to fetch config information 

```
curl -X GET http://localhost:8888/live-score-service/default
```


### service-registry application

create a new module 'service-registry' from Spring Initializer. 

```
	<groupId>org.springmeetup</groupId>
	<artifactId>service-registry</artifactId>
```

in the dependencies section, select 'Eureka Server'. eventually following dependency should be in pom.xml.  

```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
		</dependency>
```

add '@EnableEurekaServer' annotation to 'ServiceRegistryApplication.java'. should look like this

```
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceRegistryApplication.class, args);
	}

}
```

edit 'application.properties' file so that server port is defined.

```

spring.application.name=service-registry
server.port=8760

eureka.client.register-with-eureka=false
```

run 'ServiceRegistryApplication.java' as Java application from your IDE or following

```
cd service-registry
mvn spring-boot:run
```

you should be able to see Eureka main page from your favourite browser

```
http://localhost:8760/
```

### live-score-service application

for now let's leave the config-service and service-registry apps up and running for a while aside. we'll revisit them during the workshop. 

live-score-service is the main module in this workshop. this is where we will mainly add following features 

+ traditional web REST service 
+ make it reactive with webflux
+ play with Flux<Greeting> a bit
+ create consumer driven contracts and do the implementation according to the contract
+ fetch data from Redis in a recactive manner
+ stream data from Kafka in a reactive manner as SSEs
+ integrate with config-service
+ integrate with service-registry

create a new module 'live-score-service' from Spring Initializer

```
	<groupId>org.springmeetup</groupId>
	<artifactId>live-score-service</artifactId>
```

select following dependencies
+ Reactive Web (Reactive web application with Spring WebFlux and Netty)
+ DevTools
+ Lombok
+ Actuator
+ Cloud Contract Verifier
+ Eureka Discovery
+ Config Client
+ Reactive Redis
+ Kafka (Kafka messaging support using Spring Kafka)

when project is created, there will be quite a few dependencies in pom.xml

add following pieces manually

```
	<properties>
		<spring-cloud-contract.version>2.1.1.RELEASE</spring-cloud-contract.version>
	</properties>
```

```
    <dependencies>
        ...
		<dependency>
			<groupId>io.projectreactor.kafka</groupId>
			<artifactId>reactor-kafka</artifactId>
		</dependency>
		
		<!-- Micormeter core dependecy  -->
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-core</artifactId>
		</dependency>
		<!-- Micrometer Prometheus registry  -->
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-registry-prometheus</artifactId>
		</dependency>
		...
		
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>

			<plugin>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-contract-maven-plugin</artifactId>
				<version>${spring-cloud-contract.version}</version>
				<extensions>true</extensions>
				<configuration>
					<packageWithBaseClasses>org.springmeetup.livescoreservice</packageWithBaseClasses>
					<testMode>EXPLICIT</testMode>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

#### contract driven REST development

we will implement 'GET /match/{id}' REST service in the contract driven manner

the steps for making a contract driven service development includes following steps

+ defining the contract 

```
package contracts.api

import org.springframework.cloud.contract.spec.Contract

Contract.make {
	description("Returns the details of match by id : 1")

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
```

+ generating the test classes automatically

which will be handled by spring-cloud-contract-maven-plugin
```
			<plugin>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-contract-maven-plugin</artifactId>
				<version>${spring-cloud-contract.version}</version>
				<extensions>true</extensions>
				<configuration>
					<packageWithBaseClasses>org.springmeetup.livescoreservice</packageWithBaseClasses>
					<testMode>EXPLICIT</testMode>
				</configuration>
			</plugin>
```

+ add ApiBase.java test class for ApiTest.java compile problem

```
@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "server.port=0")
public class ApiBase {

	@LocalServerPort
	int port;
	
	...
	...
	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost:" + this.port;
    }	
```

+ do the actual RestController implementation to fix the test problems

pay attention to following files

```
src/test/resources/contracts/api/get-match-by-id-1.groovy
target/generated-test-sources/contracts/org/springmeetup/livescoreservice/ApiTest.java
src/test/java/org/springmeetup/livescoreservice/ApiBase.java
src/main/java/org/springmeetup/livescoreservice/rest/ApiRestController.java
src/main/resources/application.properties
```
 
as you progress, you can run tests with following command 

```
cd live-score-service
mvn test
```

when the first contract is successfully implemented and the test passes, we can focus on the second contract

```
src/test/resources/contracts/api/get-match-by-id-2.groovy
```

#### reactive Redis integration

after we have both tests pass, we will implement Redis integration so that the match data can be persisted to Redis and fetched from Redis in a reactive manner

```
src/main/java/org/springmeetup/livescoreservice/redis/RedisConfiguration.java
src/main/java/org/springmeetup/livescoreservice/service/ApiRestService.java
```

once we have the implementation ready, we should Run 'LiveScoreServiceApplication.java' from IDE or similar to previous sections : 'mvn spring-boot:run'

```
curl -X POST \
  http://localhost:8080/match \
  -H 'Content-Type: application/json' \
  -d '{
	"match-id": 3,
	"name": "Barcelona - Getafe",
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
		},
		{
			"minute": 78, 
			"type": "YELLOW",
			"team": "Real Madrid",						
			"player-name": "Luka Modric"
		}
	]
}'
```

```
curl -X GET http://localhost:8080/match/3
```

should return saved Match data

#### reactive Kafka integration

in order to stream Match data as Server Sent Events through Flux<Match>, we will use Kafka. 

```
src/main/java/org/springmeetup/livescoreservice/kafka/KafkaConfiguration.java
src/main/java/org/springmeetup/livescoreservice/kafka/KafkaService.java
src/main/java/org/springmeetup/livescoreservice/service/ApiRestService.java
src/main/java/org/springmeetup/livescoreservice/rest/ApiRestController.java
```

once the KafkaSender, KafkaReceiver beans are configured properly and implementations are finalized, you can try

```
curl -X GET http://localhost:8080/match/2/stream 
```

the connection will remain open until one of the parties cancels the connection and any update on the match with id '2' will be published to this client


### team-service application

this application has nothing special, it's just used for routing requests from gateway-service. so do not hesitate and copy /paste the whole project. 

run 'TeamServiceApplication.java'

```
curl -X GET http://localhost:8081/team/Fenerbahçe
```

should return information for team 'Fenerbahçe'

### gateway-service application

create a new module 'gateway-service' from Spring Initializer

```
	<groupId>org.springmeetup</groupId>
	<artifactId>gateway-service</artifactId>
```

select following dependencies
+ Eureka Discovery
+ Zuul

in the dependencies section, select 'Config Server'. eventually following dependency should be in pom.xml.  

```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
```

add '@EnableZuulProxy' annotation to 'GatewayServiceApplication.java'. should look like this

```
@SpringBootApplication
@EnableZuulProxy
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}
```

edit 'application.properties' file so that server port, eureka server address and routing roules are defined. 

```
spring.application.name=gateway-service
server.port=8000

eureka.client.service-url.defaultZone=http://localhost:8760/eureka/

zuul.routes.team.path=/team-service
zuul.routes.team.serviceId=team-service

zuul.routes.livescore.path=/live-score-service
zuul.routes.livescore.serviceId=live-score-service
```

run 'GatewayServiceApplication.java' as Java application from your IDE or following

```
cd gateway-service
mvn spring-boot:run
```

try to fetch live-score-service and team-service 

```
curl -X GET http://localhost:8000/live-score-service/match/3
curl -X GET http://localhost:8000/team-service/team/Fenerbahçe
```

also try non-existing urls and see the failure with 404

```
curl -X GET http://localhost:8000/non-existing-service/match/3
curl -X GET http://localhost:8000/live-score-service/non-existing-path/3
```

### live-score-service prometheus exporter

having the required dependencies and configuration you should be able to query prometheus exporter 

```
management.endpoints.web.exposure.include=*
```

```
curl -X GET http://localhost:8080/actuator/prometheus
```
