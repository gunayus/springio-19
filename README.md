
# Cloud-native, Reactive, live score streaming app development workshop
This repository contains the source code and the resources for workshop from Spring IO 2019, Barcelona.

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

### Installing Kafka
Download latest kafka release as explained in 

https://kafka.apache.org/quickstart

```
> tar -xzf kafka_2.12-2.2.0.tgz
> cd kafka_2.12-2.2.0
> bin/zookeeper-server-start.sh config/zookeeper.properties
> bin/kafka-server-start.sh config/server.properties
```

