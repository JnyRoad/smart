//package com.tce.smart.platform.core.config;
//
//import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
//import org.apache.ibatis.reflection.MetaObject;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
///**
// * 根据表生成默认插入的字段
// *
// * @author
// * @date
// */
//@Component
//public class MyBatisPlusObjectHandle implements MetaObjectHandler {
//
//    @Override
//    public void insertFill(MetaObject metaObject) {
//        setFieldValByName("createTime", LocalDateTime.now(),metaObject);
//        setFieldValByName("updateTime",LocalDateTime.now(),metaObject);
//    }
//
//    @Override
//    public void updateFill(MetaObject metaObject) {
//        setFieldValByName("updateTime",LocalDateTime.now(),metaObject);
//    }
//}