package com.tce.smart.data.config;


import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/***
 * description:  DHR-查询视图数据源 <br>
 * date: 2021/05/27
 * author: wuling
 */
@Configuration
@ComponentScan(value = {"com.tce.smart.dhrview.core.service"})
@MapperScan(basePackages = {"com.tce.smart.dhrview.core.mapper"}, sqlSessionTemplateRef =
		"dhrviewSqlSessionTemplate")
public class DHRViewDataSourceConfig {

	/**
	 * 获取数据源
	 *
	 * @return DataSource
	 */
	@Bean(name = "dhrviewDataSource")
	@ConfigurationProperties(prefix = "dhrview.datasource")
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
	@Bean(name = "dhrviewSqlSessionFactory")
	public SqlSessionFactory getSqlSessionFactory(@Qualifier("dhrviewDataSource") DataSource dataSource) throws Exception {
		MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		sqlSessionFactory.setDataSource(dataSource);
		//分页插件
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
		paginationInterceptor.setLimit(-1);
		sqlSessionFactory.setPlugins(new Interceptor[]{paginationInterceptor});
		//添加xml路径
		sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml"));
		return sqlSessionFactory.getObject();
	}

	/**
	 * 数据源事务管理器
	 *
	 * @param dataSource
	 * @return
	 */
	@Bean(name = "dhrviewTransactionManager")
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("dhrviewDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 数据源 dhrviewSqlSessionTemplate
	 *
	 * @param sqlSessionFactory
	 * @return
	 */
	@Bean(name = "dhrviewSqlSessionTemplate")
	public SqlSessionTemplate getSqlSessionTemplate(@Qualifier("dhrviewSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
