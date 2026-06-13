package com.tce.smart.transfer.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"com.tce.smart.transfer.dao1"}, sqlSessionFactoryRef = "platform1SqlSessionFactory")
public class Platform1DataSourceConfig {

	/**
	 * 获取数据源
	 *
	 * @return DataSource
	 */
	@Bean(name = "platform1DataSource")
	@ConfigurationProperties(prefix = "platform1.datasource")
	public DataSource getDataSource() {
		return new DruidDataSource();
	}


	/**
	 * 获取数据源的sqlSessionFactory
	 *
	 * @param dataSource
	 * @return
	 * @throws Exception
	 */
	@Bean(name = "platform1SqlSessionFactory")
	@Primary
	public SqlSessionFactory getSqlSessionFactory(@Qualifier("platform1DataSource") DataSource dataSource) throws Exception {
		//创建SqlSessionFactoryBean
		SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
		//注意：如果需要兼容mybatis-plus需要使用MybatisSqlSessionFactoryBean 代替 SqlSessionFactoryBean
		//MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		//设置DataSource
		sqlSessionFactory.setDataSource(dataSource);
		//添加xml路径
		sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml"));
		//添加model路径
//		sqlSessionFactory.setTypeAliasesPackage("com.tce.smart.platform.core.mapper");

		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setCallSettersOnNulls(true);
		sqlSessionFactory.setConfiguration(configuration);

		return sqlSessionFactory.getObject();
	}


	/**
	 * 数据源事务管理器
	 *
	 * @param dataSource
	 * @return
	 */
	@Bean(name = "platform1TransactionManager")
	@Primary
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("platform1DataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 数据源 sqlSessionTemplate
	 *
	 * @param sqlSessionFactory
	 * @return
	 */
	@Bean(name = "platform1SqlSessionTemplate")
	@Primary
	public SqlSessionTemplate getSqlSessionTemplate(@Qualifier("platform1SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
