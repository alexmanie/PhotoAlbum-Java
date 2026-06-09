# Step-by-Step Guide: Migrating Java Applications from RabbitMQ to Azure Service Bus

## Overview

This guide walks through migrating a Java (Spring Boot) application that uses RabbitMQ for messaging to **Azure Service Bus**. Azure Service Bus is a fully managed enterprise message broker with queues and publish-subscribe topics, offering a cloud-native alternative to RabbitMQ.

---

## Prerequisites

- Java 11+ and Maven or Gradle
- An active [Azure subscription](https://azure.microsoft.com/free/)
- Azure CLI installed (`az --version`)
- Existing Spring Boot application using Spring AMQP / RabbitMQ

---

## Step 1: Provision Azure Service Bus

### 1.1 Create a Service Bus Namespace

```bash
az servicebus namespace create \
  --name <your-namespace> \
  --resource-group <your-resource-group> \
  --location eastus \
  --sku Standard
```

### 1.2 Create Queues (replacing RabbitMQ queues)

```bash
az servicebus queue create \
  --name <your-queue-name> \
  --namespace-name <your-namespace> \
  --resource-group <your-resource-group>
```

### 1.3 Create Topics and Subscriptions (replacing RabbitMQ exchanges/fanout)

```bash
# Create a topic
az servicebus topic create \
  --name <your-topic-name> \
  --namespace-name <your-namespace> \
  --resource-group <your-resource-group>

# Create a subscription on the topic
az servicebus topic subscription create \
  --name <your-subscription-name> \
  --topic-name <your-topic-name> \
  --namespace-name <your-namespace> \
  --resource-group <your-resource-group>
```

### 1.4 Retrieve the Connection String

```bash
az servicebus namespace authorization-rule keys list \
  --name RootManageSharedAccessKey \
  --namespace-name <your-namespace> \
  --resource-group <your-resource-group> \
  --query primaryConnectionString -o tsv
```

---

## Step 2: Update Maven/Gradle Dependencies

### Remove RabbitMQ Dependencies

**pom.xml — remove:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### Add Azure Service Bus Dependencies

**pom.xml — add:**
```xml
<!-- Azure Service Bus with Spring integration -->
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-servicebus-jms</artifactId>
    <version>5.x.x</version>
</dependency>

<!-- Or use the Azure Service Bus SDK directly -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-servicebus</artifactId>
    <version>7.x.x</version>
</dependency>
```

> **Tip:** Use the [Spring Cloud Azure BOM](https://aka.ms/spring-cloud-azure) for managed dependency versions:
> ```xml
> <dependencyManagement>
>   <dependencies>
>     <dependency>
>       <groupId>com.azure.spring</groupId>
>       <artifactId>spring-cloud-azure-dependencies</artifactId>
>       <version>5.x.x</version>
>       <type>pom</type>
>       <scope>import</scope>
>     </dependency>
>   </dependencies>
> </dependencyManagement>
> ```

---

## Step 3: Update Application Configuration

### Remove RabbitMQ configuration from `application.properties`:

```properties
# REMOVE these RabbitMQ settings
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

### Add Azure Service Bus configuration:

```properties
# Azure Service Bus — JMS approach (recommended for Spring Boot)
spring.jms.servicebus.connection-string=<your-connection-string>
spring.jms.servicebus.idle-timeout=1800000
spring.jms.servicebus.pricing-tier=standard

# OR use environment variable / Azure Key Vault reference (recommended for production):
# spring.jms.servicebus.connection-string=${AZURE_SERVICEBUS_CONNECTION_STRING}
```

---

## Step 4: Replace RabbitMQ Configuration Beans

### Before (RabbitMQ):

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue myQueue() {
        return new Queue("my-queue", true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("my-exchange");
    }

    @Bean
    public Binding binding(Queue myQueue, TopicExchange exchange) {
        return BindingBuilder.bind(myQueue).to(exchange).with("routing.key");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
```

### After (Azure Service Bus via JMS):

```java
@Configuration
@EnableJms
public class ServiceBusConfig {

    // No explicit queue or topic bean definitions needed.
    // Queues/topics are provisioned in Azure (Step 1).
    // JmsTemplate is auto-configured by Spring Cloud Azure.
}
```

---

## Step 5: Replace Message Producers

### Before (RabbitMQ):

```java
@Service
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend("my-exchange", "routing.key", message);
    }
}
```

### After (Azure Service Bus — JMS):

```java
@Service
public class MessageProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void sendToQueue(String message) {
        jmsTemplate.convertAndSend("my-queue", message);
    }

    public void sendToTopic(String message) {
        jmsTemplate.convertAndSend("my-topic", message);
    }
}
```

### After (Azure Service Bus — SDK sender, for more control):

```java
@Service
public class MessageProducer {

    private final ServiceBusSenderClient senderClient;

    public MessageProducer(@Value("${azure.servicebus.connection-string}") String connectionString,
                           @Value("${azure.servicebus.queue-name}") String queueName) {
        this.senderClient = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }

    public void sendMessage(String body) {
        ServiceBusMessage message = new ServiceBusMessage(body);
        senderClient.sendMessage(message);
    }
}
```

---

## Step 6: Replace Message Consumers

### Before (RabbitMQ):

```java
@Component
public class MessageConsumer {

    @RabbitListener(queues = "my-queue")
    public void receiveMessage(String message) {
        System.out.println("Received: " + message);
    }
}
```

### After (Azure Service Bus — JMS):

```java
@Component
public class MessageConsumer {

    @JmsListener(destination = "my-queue")
    public void receiveMessage(String message) {
        System.out.println("Received: " + message);
    }
}
```

### After (Azure Service Bus — SDK processor, for more control):

```java
@Component
public class MessageConsumer implements InitializingBean, DisposableBean {

    private ServiceBusProcessorClient processorClient;

    @Autowired
    private Environment env;

    @Override
    public void afterPropertiesSet() {
        processorClient = new ServiceBusClientBuilder()
                .connectionString(env.getProperty("azure.servicebus.connection-string"))
                .processor()
                .queueName(env.getProperty("azure.servicebus.queue-name"))
                .processMessage(this::handleMessage)
                .processError(ctx -> System.err.println("Error: " + ctx.getException()))
                .buildProcessorClient();
        processorClient.start();
    }

    private void handleMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        System.out.println("Received: " + message.getBody().toString());
        context.complete(); // Settle the message
    }

    @Override
    public void destroy() {
        if (processorClient != null) processorClient.close();
    }
}
```

---

## Step 7: Handle Dead-Letter Queues (DLQ)

Azure Service Bus has built-in dead-letter queue support. Messages that fail processing are automatically moved to `<queue-name>/$DeadLetterQueue`.

```java
// Read from the DLQ
ServiceBusReceiverClient dlqReceiver = new ServiceBusClientBuilder()
    .connectionString(connectionString)
    .receiver()
    .queueName(queueName)
    .subQueue(SubQueue.DEAD_LETTER)
    .buildClient();
```

Map RabbitMQ DLX (dead-letter exchange) settings:

| RabbitMQ                        | Azure Service Bus                        |
|---------------------------------|------------------------------------------|
| `x-dead-letter-exchange`        | Built-in DLQ (automatic)                 |
| `x-message-ttl`                 | Queue `defaultMessageTimeToLive`         |
| `x-max-length` / overflow       | Queue `maxSizeInMegabytes`               |

---

## Step 8: Migrate Message Properties and Headers

| RabbitMQ Property          | Azure Service Bus Equivalent            |
|----------------------------|-----------------------------------------|
| `MessageProperties`        | `ServiceBusMessage` properties          |
| `correlationId`            | `message.setCorrelationId(...)`         |
| `contentType`              | `message.setContentType(...)`           |
| Custom headers             | `message.getApplicationProperties().put(...)` |
| `expiration` (TTL)         | `message.setTimeToLive(Duration)`       |
| `messageId`                | `message.setMessageId(...)`             |

```java
ServiceBusMessage message = new ServiceBusMessage("Hello, Azure!");
message.setCorrelationId("corr-123");
message.setContentType("application/json");
message.setTimeToLive(Duration.ofMinutes(10));
message.getApplicationProperties().put("customHeader", "value");
```

---

## Step 9: Update Security and Authentication (Recommended: Managed Identity)

Instead of connection strings, use **Azure Managed Identity** for passwordless authentication in production.

```properties
# application.properties — Managed Identity (no secrets needed)
spring.jms.servicebus.namespace=<your-namespace>
spring.jms.servicebus.credential.managed-identity-enabled=true
```

Or with the SDK:

```java
ServiceBusSenderClient sender = new ServiceBusClientBuilder()
    .credential("<your-namespace>.servicebus.windows.net",
                new DefaultAzureCredentialBuilder().build())
    .sender()
    .queueName("my-queue")
    .buildClient();
```

Grant the app's managed identity the **Azure Service Bus Data Sender** and **Data Receiver** roles via IAM in the Azure portal.

---

## Step 10: Test and Validate

1. **Unit tests** — Update mocks: replace `RabbitTemplate` mocks with `JmsTemplate` or `ServiceBusSenderClient` mocks.
2. **Integration tests** — Use [Azure Service Bus emulator](https://learn.microsoft.com/azure/service-bus-messaging/overview-emulator) or a dedicated test namespace.
3. **Validate message flow** — Use the Azure Portal → Service Bus → Service Bus Explorer to inspect queues and topics.
4. **Monitor** — Enable Azure Monitor and Application Insights for metrics, dead-letter alerts, and tracing.

---

## Step 11: Cleanup

Once the migration is verified in production:

1. Remove all `spring-boot-starter-amqp` dependencies and RabbitMQ beans.
2. Delete RabbitMQ-related configuration classes and properties.
3. Decommission RabbitMQ infrastructure.

---

## RabbitMQ to Azure Service Bus — Concept Mapping Summary

| RabbitMQ Concept         | Azure Service Bus Equivalent         |
|--------------------------|--------------------------------------|
| Queue                    | Queue                                |
| Exchange (direct/topic)  | Topic                                |
| Binding / Routing key    | Subscription with filter rules       |
| Fanout exchange          | Topic with multiple subscriptions    |
| Virtual host (vhost)     | Namespace                            |
| Consumer acknowledgement | `context.complete()` / `abandon()`   |
| Message TTL              | `TimeToLive` on message or queue     |
| Dead-letter exchange     | Built-in Dead-Letter Queue (DLQ)     |

---

## References

- [Azure Service Bus Documentation](https://learn.microsoft.com/azure/service-bus-messaging/)
- [Spring Cloud Azure — Service Bus JMS](https://aka.ms/spring-cloud-azure-servicebus-jms)
- [Azure Service Bus SDK for Java](https://learn.microsoft.com/java/api/overview/azure/messaging-servicebus-readme)
- [Azure Service Bus Emulator](https://learn.microsoft.com/azure/service-bus-messaging/overview-emulator)
- [Managed Identity with Service Bus](https://learn.microsoft.com/azure/service-bus-messaging/service-bus-managed-service-identity)

