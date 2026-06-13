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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/***
 * description:  许昌C6数据源配置
 * date: 2019/12/17 8:53 <br>
 */
@Configuration
@ConditionalOnProperty(name = "xc-c6.datasource.type",havingValue = "com.alibaba.druid.pool.DruidDataSource")
@ComponentScan("com.tce.smart.xcc6.core.service.impl")
@MapperScan(basePackages = {"com.tce.smart.xcc6.core.mapper"}, sqlSessionTemplateRef = "xcc6SqlSessionTemplate")
public class XCC6DataSourceConfig {

	/**
	 * 获取数据源
	 *
	 * @return DataSource
	 */
	@Bean(name = "xcc6DataSource")
	@ConfigurationProperties(prefix = "xc-c6.datasource")
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
	@Bean(name = "xcc6SqlSessionFactory")
	public SqlSessionFactory getSqlSessionFactory(@Qualifier("xcc6DataSource") DataSource dataSource) throws Exception {
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
	@Bean(name = "xcc6TransactionManager")
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("xcc6DataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 数据源 xcc6SqlSessionTemplate
	 *
	 * @param sqlSessionFactory
	 * @return
	 */
	@Bean(name = "xcc6SqlSessionTemplate")
	public SqlSessionTemplate getSqlSessionTemplate(@Qualifier("xcc6SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
