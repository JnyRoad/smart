package com.tce.smart.common.core.event;

import com.tce.smart.common.core.model.SmartEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: EventPublisher
 * @Author jinbo
 * @Date 2019/5/10
 */
@Component
public class SmartEventPublisher {
    @Resource
    private ApplicationContext applicationContext;

    public void publish(Object message) {
        applicationContext.publishEvent(new SmartEvent<>(this, message));
    }
}
