package com.fanzhang.framework.tx;

import com.fanzhang.framework.tx.localmessagetable.TXLocalMessageTableAutoConfiguration;
import com.fanzhang.framework.tx.rocketmq.DefaultRocketMQLocalTransactionListener;
import com.fanzhang.framework.tx.rocketmq.RocketMQTXTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;

import java.util.List;

/**
 * 事务自动配置
 *
 * @author Zhang Fan
 */
@AutoConfigureAfter(TXLocalMessageTableAutoConfiguration.class)
@Configuration
public class TXAutoConfiguration {

    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnMissingBean
    @Bean
    public RocketMQTXTemplate rocketMQTXTemplate(RocketMQTemplate rocketMQTemplate, ObjectMapper objectMapper) {
        CompositeMessageConverter messageConverter = (CompositeMessageConverter) rocketMQTemplate.getMessageConverter();
        List<MessageConverter> converters = messageConverter.getConverters();
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2MessageConverter) {
                ((MappingJackson2MessageConverter) converters.get(i)).setObjectMapper(objectMapper);
                break;
            }
        }
        return new RocketMQTXTemplate();
    }

    @ConditionalOnClass(RocketMQTXTemplate.class)
    @Bean
    public RocketMQLocalTransactionListener rocketMQLocalTransactionListener() {
        return new DefaultRocketMQLocalTransactionListener();
    }

    @Primary
    @Bean
    public TXTemplate txTemplate() {
        return new DefaultTXTemplate();
    }

}
