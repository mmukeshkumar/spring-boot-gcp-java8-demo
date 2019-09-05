package com.example.springboot.gcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Column;
import org.springframework.cloud.gcp.data.spanner.core.mapping.PrimaryKey;
import org.springframework.cloud.gcp.data.spanner.core.mapping.Table;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SpringBootApplication
//Based of
// 1) https://github.com/joshlong/bootiful-gcp
//2) https://github.com/spring-cloud/spring-cloud-gcp/tree/master/spring-cloud-gcp-samples/spring-cloud-gcp-data-spanner-sample
//3) https://youtu.be/2Jo3vy7iQf8
//4)https://codelabs.developers.google.com/codelabs/cloud-app-engine-springboot/index.html?index=..%2F..index#0
public class GcpDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GcpDemoApplication.class, args);
    }
}


@RestController
class ProducerRestController {

    private final PubSubTemplate template;

    ProducerRestController(PubSubTemplate template) {
        this.template = template;
    }

    @PostMapping("/greet/{message}")
    ListenableFuture<String> greet(@PathVariable String message) {
        return this.template.publish("greetings-topic", "greetings Message: " + message + "!");
    }
}


@Log4j2
@Component
// This will run on application startup
class ConsumerRunner implements ApplicationRunner {

    private final PubSubTemplate pubSubTemplate;

    ConsumerRunner(PubSubTemplate pubSubTemplate) {
        this.pubSubTemplate = pubSubTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        pubSubTemplate.subscribe("greetings-subscription", new Consumer<BasicAcknowledgeablePubsubMessage>() {
            @Override
            public void accept(BasicAcknowledgeablePubsubMessage basicAcknowledgeablePubsubMessage) {
                log.info("Received message: " + basicAcknowledgeablePubsubMessage.getPubsubMessage().toString() );
                basicAcknowledgeablePubsubMessage.ack();
            }
        });
    }
}


@Component
// This will run on application startup
class SpannerOrdersRunner implements ApplicationRunner {

    private final OrderRepository repo;

    SpannerOrdersRunner(OrderRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        repo.deleteAll();
        Stream.of("Mukesh", "Zippy")
                .map(name -> new Order(UUID.randomUUID().toString(), name))
                .map(repo::save)
                .forEach(System.out::println);
    }
}


@RepositoryRestResource
// Adding this @RepositoryRestResource annotation will provide CRUD access to the data in the Orders table
interface OrderRepository extends PagingAndSortingRepository<Order, String> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Orders")
class Order {
    @Id
    @PrimaryKey
    private String id;

    @Column(name = "order_name")
    private String orderName;

}
