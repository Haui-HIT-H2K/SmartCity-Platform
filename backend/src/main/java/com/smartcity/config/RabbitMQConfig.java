package com.smartcity.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ Configuration
 * Cấu hình kết nối tới 2 RabbitMQ Edge Nodes:
 * - Edge Node 1: rabbit-edge-1
 * - Edge Node 2: rabbit-edge-2
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    // Edge 1 Configuration
    @Value("${rabbitmq.edge1.host}")
    private String edge1Host;
    
    @Value("${rabbitmq.edge1.port}")
    private int edge1Port;
    
    @Value("${rabbitmq.edge1.username}")
    private String edge1Username;
    
    @Value("${rabbitmq.edge1.password}")
    private String edge1Password;
    
    @Value("${rabbitmq.edge1.queue-name}")
    private String edge1QueueName;

    // Edge 2 Configuration
    @Value("${rabbitmq.edge2.host}")
    private String edge2Host;
    
    @Value("${rabbitmq.edge2.port}")
    private int edge2Port;
    
    @Value("${rabbitmq.edge2.username}")
    private String edge2Username;
    
    @Value("${rabbitmq.edge2.password}")
    private String edge2Password;
    
    @Value("${rabbitmq.edge2.queue-name}")
    private String edge2QueueName;

    /**
     * ConnectionFactory cho Edge Node 1
     */
    @Bean(name = "edge1ConnectionFactory")
    @Primary
    public ConnectionFactory edge1ConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(edge1Host);
        factory.setPort(edge1Port);
        factory.setUsername(edge1Username);
        factory.setPassword(edge1Password);
        
        log.info("Configured Edge1 ConnectionFactory: {}:{}", edge1Host, edge1Port);
        
        return factory;
    }

    /**
     * Message Converter - JSON to Object
     * Converts JSON messages from Python simulator to Java objects
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate cho Edge Node 1
     */
    @Bean(name = "edge1RabbitTemplate")
    @Primary
    public RabbitTemplate edge1RabbitTemplate(
            @Qualifier("edge1ConnectionFactory") ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setDefaultReceiveQueue(edge1QueueName);
        template.setMessageConverter(messageConverter); // Enable JSON deserialization
        
        log.info("Created Edge1 RabbitTemplate for queue: {}", edge1QueueName);
        
        return template;
    }

    /**
     * Queue cho Edge Node 1
     */
    @Bean(name = "edge1Queue")
    public Queue edge1Queue() {
        return new Queue(edge1QueueName, true); // durable = true
    }

    /**
     * AmqpAdmin cho Edge Node 1
     */
    @Bean(name = "edge1AmqpAdmin")
    public AmqpAdmin edge1AmqpAdmin(
            @Qualifier("edge1ConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * ConnectionFactory cho Edge Node 2
     */
    @Bean(name = "edge2ConnectionFactory")
    public ConnectionFactory edge2ConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(edge2Host);
        factory.setPort(edge2Port);
        factory.setUsername(edge2Username);
        factory.setPassword(edge2Password);
        
        log.info("Configured Edge2 ConnectionFactory: {}:{}", edge2Host, edge2Port);
        
        return factory;
    }

    /**
     * RabbitTemplate cho Edge Node 2
     */
    @Bean(name = "edge2RabbitTemplate")
    public RabbitTemplate edge2RabbitTemplate(
            @Qualifier("edge2ConnectionFactory") ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setDefaultReceiveQueue(edge2QueueName);
        template.setMessageConverter(messageConverter); // Enable JSON deserialization
        
        log.info("Created Edge2 RabbitTemplate for queue: {}", edge2QueueName);
        
        return template;
    }

    /**
     * Queue cho Edge Node 2
     */
    @Bean(name = "edge2Queue")
    public Queue edge2Queue() {
        return new Queue(edge2QueueName, true); // durable = true
    }

    /**
     * AmqpAdmin cho Edge Node 2
     */
    @Bean(name = "edge2AmqpAdmin")
    public AmqpAdmin edge2AmqpAdmin(
            @Qualifier("edge2ConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
