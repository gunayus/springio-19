
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

add '@EnableConfigServer' annotation to 'ConfigServiceApplication.java'. shuld look like this

```
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServiceApplication.class, args);
	}

}
```

