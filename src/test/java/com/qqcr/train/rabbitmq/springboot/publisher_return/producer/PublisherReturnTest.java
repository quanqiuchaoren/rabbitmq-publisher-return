package com.qqcr.train.rabbitmq.springboot.publisher_return.producer;

import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PublisherReturnTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @After
    public void after() throws InterruptedException {
        /*
        在测试方法中，防止资源被释放，导致exchange分发消息的情况不被这里的客户端代码获取，进而导致所有的publisher return回调不执行
         */
        TimeUnit.SECONDS.sleep(2);
    }

    @DisplayName("有exchange收到了消息，消息被转发给了queue，则publisher return回调不会被执行")
    @Test
    public void should_not_be_called_when_queue_received_message() {
        /* 给template设置return回调函数 */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("publisher return 回调被执行了。。。。。。。。"); // 这里不会打印
                throw new RuntimeException("如果这里被执行，则证明不正常，因为消息被exchange转发给了queue");
            }
        });
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "key_return", "publisher return mq hello~~~");
    }

    @DisplayName("有exchange收到消息，消息没有被转发给queue")
    @Test
    public void should_be_called_when_message_is_not_redirected_to_queue_by_exchange() {
        /* 给template设置return回调函数 */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             *
             * @param message the returned message. 被返回的消息的内容，也就是发送的消息的内容
             * @param replyCode the reply code. 返回码，类似于错误码
             * @param replyText the reply text. 返回信息，类似于错误信息
             * @param exchange the exchange. 发送的
             * @param routingKey the routing key.
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                /*
                 * 下面返回的信息中，312代表消息到达了交换机，但是没有被交换机分发到任何queue
                 * publisher_return_queue是发送消息的时候，制定的交换机
                 * not_existed_routing_key是发送消息的时候制定的key
                 */
                System.out.println("publisher return 回调被执行了。。。。。。。。"); // 这里会打印，因为消息没有被exchange转发到任何queue
                System.out.println("message: " + message); // (Body:'publisher return mq hello~~~' MessageProperties [headers={}, contentType=text/plain, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, deliveryTag=0])
                System.out.println("replyCode: " + replyCode); // replyCode: 312
                System.out.println("replyText: " + replyText); // replyText: NO_ROUTE
                System.out.println("exchange: " + exchange); // exchange: publisher_return_queue
                System.out.println("routingKey: " + routingKey); // routingKey: not_existed_routing_key
            }
        });
        // 下面的路由键没有与任何queue绑定
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "not_existed_routing_key", "publisher return mq hello~~~");
    }

    @DisplayName("没有exchange收到消息")
    @Test
    public void should_xxx() {
        /* 给template设置return回调函数 */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("publisher return 回调被执行了。。。。。。。。"); // 这里不会打印，消息没有被发送到任何exchange，所以也就不存在消息没有被exchange转发的情况
                throw new RuntimeException("如果这里被执行，则证明不正常，因为消息没有到达任何exchange");
            }
        });
        // 下面的路由键没有与任何queue绑定
        rabbitTemplate.convertAndSend("not_existed_exchange_name", "not_existed_routing_key", "publisher return mq hello~~~");
    }
}
