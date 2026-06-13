package com.tce.smart.platform.core.config;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.tce.smart.common.security.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 9:26
 */
@Slf4j
@Configuration
@MapperScan("com.tce.smart.platform.core.mapper")
public class DBAutoConfiguration implements MetaObjectHandler {
	@Override
	public void insertFill(MetaObject metaObject) {
		try {
			setFieldValByName("createUserId", SecurityUtils.getUser().getId(), metaObject);
		} catch (Exception e) {
		}
		Object createTime = getFieldValByName("createTime", metaObject);
		if (ObjectUtil.isNull(createTime)) {
			setFieldValByName("createTime", LocalDateTime.now(), metaObject);
		}
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		try {
			setFieldValByName("updateUserId", SecurityUtils.getUser().getId(), metaObject);
		} catch (Exception e) {
			setFieldValByName("updateUserId", 0, metaObject);
		}
		Object updateTime = getFieldValByName("updateTime", metaObject);
		if (ObjectUtil.isNull(updateTime)) {
			setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
		}
	}

	@Bean
	public ConfigurationCustomizer configurationCustomizer() {
		return new MybatisPlusCustomizers();
	}

	class MybatisPlusCustomizers implements ConfigurationCustomizer {
		@Override
		public void customize(org.apache.ibatis.session.Configuration configuration) {
			configuration.setJdbcTypeForNull(JdbcType.NULL);
		}
	}
}