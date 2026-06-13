package com.tce.smart.schedule.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
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

/***
 * description:	tce-platform数据源 <br>
 * date: 2019/12/17 10:39 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Configuration
@ComponentScan("com.tce.smart.platform.core.service.impl")
@MapperScan(basePackages = {"com.tce.smart.platform.core.mapper"}, sqlSessionTemplateRef = "platformSqlSessionTemplate")
public class PlatformDataSourceConfig {

	@Autowired
	private PaginationInterceptor paginationInterceptor;

	/**
	 * 获取数据源
	 *
	 * @return DataSource
	 */
	@Bean(name = "platformDataSource")
	@ConfigurationProperties(prefix = "platform.datasource")
	@Primary
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
	@Bean(name = "platformSqlSessionFactory")
	@Primary
	public SqlSessionFactory getSqlSessionFactory(@Qualifier("platformDataSource") DataSource dataSource) throws Exception {
		//创建SqlSessionFactoryBean
		/*SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();*/
		//注意：如果需要兼容mybatis-plus需要使用MybatisSqlSessionFactoryBean 代替 SqlSessionFactoryBean
		MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
		//设置DataSource
		sqlSessionFactory.setDataSource(dataSource);

		// Oracle数据库兼容性配置：设置jdbcTypeForNull避免"无效的列类型: 1111"错误
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.setJdbcTypeForNull(JdbcType.NULL);
		sqlSessionFactory.setConfiguration(configuration);

		//添加xml路径
		sqlSessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:/mapper/*.xml"));

		Interceptor[] plugins = {paginationInterceptor};
		sqlSessionFactory.setPlugins(plugins);
		//添加model路径
//		sqlSessionFactory.setTypeAliasesPackage("com.tce.smart.platform.core.mapper");

		return sqlSessionFactory.getObject();
	}


	/**
	 * 数据源事务管理器
	 *
	 * @param dataSource
	 * @return
	 */
	@Bean(name = "platformTransactionManager")
	@Primary
	public DataSourceTransactionManager getDataSourceTransactionManager(@Qualifier("platformDataSource") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 数据源 sqlSessionTemplate
	 *
	 * @param sqlSessionFactory
	 * @return
	 */
	@Bean(name = "platformSqlSessionTemplate")
	@Primary
	public SqlSessionTemplate getSqlSessionTemplate(@Qualifier("platformSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
