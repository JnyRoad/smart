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

/**
 * description: OA-出差数据源 <br>
 * date: 2019/12/17 10:39 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Configuration
@ComponentScan("com.tce.smart.businesstrip.core.service.impl")
@MapperScan(basePackages = {"com.tce.smart.businesstrip.core.mapper"}, sqlSessionTemplateRef = "businessTripSqlSessionTemplate")
public class BusinessTripDataSourceConfig {

	/**
	 * 获取数据源
	 *
	 * @return DataSource
	 */
	@Bean(name = "businessTripDataSource")
	@ConfigurationProperties(prefix = "businesstrip.datasource")
	public DataSource getDataSource() {
		return DataSourceBuilder.create().build();
//		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}


	/**
	 * 获取数据源的sqlSessionFactory
	 *
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */
	@Bean(name = "businessTripSqlSessionFactory")
	public SqlSessionFactory getSqlSessionFactory(@Qualifier("businessTripDataSource") DataSource dataSource) throws Exception {
		//创建SqlSessionFactoryBean
		/*SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();*/
		//注意：如果需要兼容mybatis-plus需要使用MybatisSqlSessionFactoryBean 代替 SqlSessionFactoryBean
		MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		//设置DataSource
		sqlSessionFactory.setDataSource(dataSource);

		//分页插件
		sqlSessionFactory.setPlugins(new Interceptor[]{new PaginationInterceptor()});

		//添加xml路径
		sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml"));
		//添加model路径
//		sqlSessionFactory.setTypeAliasesPackage("com.tce.smart.data.api.entity.businesstrip");

		return sqlSessionFactory.getObject();
	}


	/**
	 * 数据源事务管理器
	 *
	 * @param dataSource
	 * @return
	 */
	@Bean(name = "businessTripTransactionManager")
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("businessTripDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 数据源 sqlSessionTemplate
	 *
	 * @param sqlSessionFactory
	 * @return
	 */
	@Bean(name = "businessTripSqlSessionTemplate")
	public SqlSessionTemplate getSqlSessionTemplate(@Qualifier("businessTripSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
