package com.tce.smart.bridge.core.common;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.bridge.core.enums.ExceptionEnum;
import com.tce.smart.bridge.core.exception.WrapperException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VO 转换类的控制器
 *
 * @ClassName ControllerWrapper
 * @Auther: WangJinbo
 * @Date: 2018/8/16
 * @Description:
 **/
@Slf4j
@Component
public class ControllerWrapper implements InitializingBean, ApplicationContextAware {

    private static final Map<String, BaseWrapper> wrappers = new ConcurrentHashMap<>();

    private ApplicationContext ctx;

    @Override
    public void afterPropertiesSet() {
        final Map<String, BaseWrapper> handlerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(ctx, BaseWrapper.class, true, true);
        handlerMap.values().forEach(p -> wrappers.put(generateKey(p.getModelClass(), p.getDataClass()), p));
        log.info("加载 VO 转化器完成,共有{}个", wrappers.size());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    public <T, F> List<F> warp(List<T> model, Class<F> dataClass) {
        List<F> data = new ArrayList<>();
        if (CollUtil.isNotEmpty(model)) {
            model.forEach(t -> {
                try {
                    data.add(warp(t, dataClass));
                } catch (IOException e) {
                    throw new WrapperException(ExceptionEnum.SERVER_ERROR);
                }
            });
        }
        return data;
    }
    @SuppressWarnings("unchecked")
    public <T, F> F warp(T model, Class<F> dataClass) throws IOException {
        BaseWrapper baseWrapper = wrappers.get(generateKey(model.getClass(), dataClass));
        if (baseWrapper == null) {
            throw new WrapperException("没有找到 " + model.getClass().getName() + " 的 VO 转化器" + generateKey(model.getClass(), dataClass));
        }
        return (F) baseWrapper.warp(model);
    }

    private String generateKey(Class<?> modelClass, Class<?> dataClass) {
        return modelClass.getName() + "_" + dataClass.getName();
    }
}
