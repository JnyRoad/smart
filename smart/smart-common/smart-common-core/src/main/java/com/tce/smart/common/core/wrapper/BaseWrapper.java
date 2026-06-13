package com.tce.smart.common.core.wrapper;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * VO 转换类的 基类
 *
 * @ClassName BaseWrapper
 * @Auther: WangJinbo
 * @Date: 2018/8/16
 * @Description:
 **/
public abstract class BaseWrapper<T, F> {
    /**
     * 实体类的class
     */
    private Class<F> modelClass;
    /**
     * 转换的 VO 实体类
     */
    private Class<T> dataClass;

    public BaseWrapper() {
        initClasses();
    }

    protected abstract F warp(T model) throws IOException;

    /**
     * 获取泛型参数
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private void initClasses() {
        if (this.modelClass == null) {
            final Class c = getClass();
            final Type type = c.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                final Type[] pType = ((ParameterizedType) type)
                        .getActualTypeArguments();
                this.modelClass = (Class<F>) pType[0];
                this.dataClass = (Class<T>) pType[1];
            }
        }
    }

    public Class<F> getModelClass() {
        return modelClass;
    }

    public Class<T> getDataClass() {
        return dataClass;
    }
}
