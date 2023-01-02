package com.qqcr.train.rabbitmq.springboot.publisher_return.producer;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "publisher_return_queue";

    public static final String EXCHANGE_WITHOUT_QUEUE = "EXCHANGE_WITHOUT_QUEUE";
    public static final String QUEUE_NAME = "return_queue";

    //1.交换机
    @Bean("exchangeWithoutQueue")
    public Exchange exchangeWithoutQueue() {
        return ExchangeBuilder.directExchange(EXCHANGE_WITHOUT_QUEUE).durable(true).build();
    }

    @Bean("exchangeWithQueue")
    public Exchange exchangeWithQueue() {
        return ExchangeBuilder.directExchange(EXCHANGE_NAME).durable(true).build();
    }


    //2.Queue 队列
    @Bean("returnQueue")
    public Queue returnQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    //3. 队列和交互机绑定关系 Binding
    /*
        1. 知道哪个队列
        2. 知道哪个交换机
        3. routing key
     */
    @Bean
    public Binding bindQueueExchange(@Qualifier("returnQueue") Queue queue, @Qualifier("exchangeWithQueue") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("key_return").noargs();
    }
}
