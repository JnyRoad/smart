package com.tce.smart.data.config;


import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/***
 * description:  许昌车辆系统数据源配置
 * date: 2019/12/17 8:53 <br>
 */
@Configuration
@ConditionalOnProperty(name = "xc-vehicle.datasource.type",havingValue = "com.alibaba.druid.pool.DruidDataSource")
@ComponentScan("com.tce.smart.xcvehicle.core.service.impl")
@MapperScan(basePackages = {"com.tce.smart.xcvehicle.core.mapper"}, sqlSessionTemplateRef = "xcvehicleSqlSessionTemplate")
public class XCVehicleDataSourceConfig {

	/**
	 * 获取数据源
	 *
	 * @return DataSource
	 */
	@Bean(name = "xcvehicleDataSource")
	@ConfigurationProperties(prefix = "xc-vehicle.datasource")
	public DataSource getDataSource() {
		return DataSourceBuilder.create().build();
	}

	/**
	 * 获取数据源的sqlSessionFactory
	 *
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */
	@Bean(name = "xcvehicleSqlSessionFactory")
	public SqlSessionFactory getSqlSessionFactory(@Qualifier("xcvehicleDataSource") DataSource dataSource) throws Exception {
		//创建SqlSessionFactoryBean
		/*SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();*/
		//注意：如果需要兼容mybatis-plus需要使用MybatisSqlSessionFactoryBean 代替 SqlSessionFactoryBean
		MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		//设置DataSource
		sqlSessionFactory.setDataSource(dataSource);

		//分页插件
		sqlSessionFactory.setPlugins(new Interceptor[]{ new PaginationInterceptor()});

//		//添加xml路径
		sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml"));
//		//添加model路径
//		sqlSessionFactory.setTypeAliasesPackage("com.tce.smart.data.api.entity.consume");

		return sqlSessionFactory.getObject();
	}

	/**
	 * 数据源事务管理器
	 *
	 * @param dataSource
	 * @return
	 */
	@Bean(name = "xcvehicleTransactionManager")
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("xcvehicleDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 数据源 xcvehicleSqlSessionTemplate
	 *
	 * @param sqlSessionFactory
	 * @return
	 */
	@Bean(name = "xcvehicleSqlSessionTemplate")
	public SqlSessionTemplate getSqlSessionTemplate(@Qualifier("xcvehicleSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
