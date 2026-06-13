package com.tce.smart.common.core.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 *
 * @date 2017/10/29
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@MapperScan("com.tce.smart.*.mapper")
public class MybatisPlusConfig {


	/**
	 * SQL执行效率插件
	 */
	@Bean
	@ConditionalOnMissingBean
	public PerformanceInterceptor performanceInterceptor() {
		return new PerformanceInterceptor();
	}

	/**
	 * 分页插件
	 *
	 * @return PaginationInterceptor
	 */
	@Bean
	@ConditionalOnMissingBean
	public PaginationInterceptor paginationInterceptor() {
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
		paginationInterceptor.setLimit(-1);
		return paginationInterceptor;
	}

	/**
	 * 逻辑删除插件
	 *
	 * @return LogicSqlInjector
	 */
	@Bean
	@ConditionalOnMissingBean
	public ISqlInjector sqlInjector() {
		return new LogicSqlInjector();
	}

	@Bean
	public MetaObjectHandler metaObjectHandler() {
		return new MetaObjectHandler() {
			@Override
			public void insertFill(MetaObject metaObject) {
				this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);
				this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
			}

			@Override
			public void updateFill(MetaObject metaObject) {
				this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
			}
		};
	}
}
