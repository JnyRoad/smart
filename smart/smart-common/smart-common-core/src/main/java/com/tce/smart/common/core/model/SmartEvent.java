package com.tce.smart.common.core.model;

import org.springframework.context.ApplicationEvent;

/**
 * @Description: TODO
 * @ProjectName smart
 * @ClassName: SmartEvent
 * @Author jinbo
 * @Date 2019/5/10
 */
public class SmartEvent<T> extends ApplicationEvent {

    private static final Long serialVersionUID = 1L;

    //定义信息
    private T message;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public SmartEvent(Object source, T message) {
        super(source);
        this.message = message;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public <F> F format(Class<F> tClass){
        return tClass.cast(this.message);
    }
}
