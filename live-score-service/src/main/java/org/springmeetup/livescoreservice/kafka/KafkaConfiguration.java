package org.springmeetup.livescoreservice.kafka;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.internals.DefaultKafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.internals.DefaultKafkaSender;
import reactor.kafka.sender.internals.ProducerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {

	@Value("${kafka.bootstrap.servers}")
	String bootstrapServers;

	@Value("${kafka.livescore.topic}")
	String topicName;

	@Bean
	KafkaReceiver kafkaReceiver() {

		Map<String, Object> configProps = new HashMap<>();
		configProps.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configProps.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configProps.put( ConsumerConfig.CLIENT_ID_CONFIG, "live-score-client");
		configProps.put( ConsumerConfig.GROUP_ID_CONFIG, "live-score-group-id");
		configProps.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
 
		return new DefaultKafkaReceiver(ConsumerFactory.INSTANCE,
				ReceiverOptions.create(configProps).subscription(Arrays.asList(topicName))
		);
	}

	@Bean
	KafkaSender<String, String> kafkaSender() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		// Setting a value greater than zero will cause the client to resend any record whose send fails with a potentially transient error.
		// Note that this retry is no different than if the client resent the record upon receiving the error. Allowing retries without setting max.in.flight.requests.per.connection to 1 will potentially change
		// the ordering of records because if two batches are sent to a single partition, and the first fails and is retried but the second succeeds,
		// then the records in the second batch may appear first. Note additionall that produce requests will be failed before the number of retries has been exhausted
		// if the timeout configured by delivery.timeout.ms expires first before successful acknowledgement. Users should generally prefer to leave this config
		// unset and instead use delivery.timeout.ms to control retry behavior.
		configProps.put(ProducerConfig.RETRIES_CONFIG, 10);

		// The configuration controls the maximum amount of time the client will wait for the response of a request.
		// If the response is not received before the timeout elapses the client will resend the request if necessary
		// or fail the request if retries are exhausted.
		configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");

		//An upper bound on the time to report success or failure after a call to send() returns.
		// This limits the total time that a record will be delayed prior to sending, the time to await acknowledgement from the broker (if expected),
		// and the time allowed for retriable send failures. The producer may report failure to send a record earlier than this config if either an
		// unrecoverable error is encountered, the retries have been exhausted, or the record is added to a batch which reached an earlier delivery
		// expiration deadline. The value of this config should be greater than or equal to the sum of request.timeout.ms and linger.ms.
		configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "163850"); // 163KByte

		// wait x millis to collect scheduler data before sending
		configProps.put(ProducerConfig.LINGER_MS_CONFIG, "100");

		// 0 : The producer will not wait for any acknowledgment from the server at all. The record will be immediately added to the socket buffer and considered sent.
		// No guarantee can be made that the server has received the record in this case, and the retries configuration will not take effect
		// 1 : This will mean the leader will write the record to its local log but will respond without awaiting full acknowledgement from all followers
		// all : This means the leader will wait for the full set of in-sync replicas to acknowledge the record.
		// This guarantees that the record will not be lost as long as at least one in-sync replica remains alive.
		configProps.put(ProducerConfig.ACKS_CONFIG, "1");

		// The maximum number of unacknowledged requests the client will send on a single connection before blocking.
		// Note that if this setting is set to be greater than 1 and there are failed sends, there is a risk of message re-ordering due to retries
		// (i.e., if retries are enabled).
		configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1");

		return new DefaultKafkaSender<>(ProducerFactory.INSTANCE,
				SenderOptions.create(configProps));
	}
}