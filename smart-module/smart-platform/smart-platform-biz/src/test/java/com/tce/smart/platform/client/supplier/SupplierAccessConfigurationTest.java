package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.client.identity.ClientPersonnelDirectory;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import javax.sql.DataSource;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 仅装配本包Spring配置，确认关闭时无入口、开启时使用真实仓储适配；不启动平台应用。 */
public class SupplierAccessConfigurationTest {
    @Test public void absentConfigurationDoesNotExposeEndpointOrRequireDataSource() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(SupplierAccessConfiguration.class, SupplierAccessController.class);
            context.refresh();
            assertTrue(context.getBeansOfType(SupplierAccessController.class).isEmpty());
            assertTrue(context.getBeansOfType(SupplierAccessRepository.class).isEmpty());
        }
    }

    @Test public void enabledConfigurationWiresRealJdbcAdapterWithoutOpeningDatabase() {
        DataSource dataSource = mock(DataSource.class);
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            Map<String, Object> config = new HashMap<>();
            config.put("smart.client.supplier.enabled", "true");
            config.put("smart.client.supplier.posts[0].id", "gate");
            config.put("smart.client.supplier.posts[0].name", "合成岗位");
            config.put("smart.client.supplier.posts[0].park-id", "1");
            config.put("smart.client.supplier.posts[0].park-name", "合成园区");
            config.put("smart.client.supplier.posts[0].area-id", "area");
            config.put("smart.client.supplier.posts[0].area-name", "合成区域");
            config.put("smart.client.supplier.posts[0].admittance-area-type-code", "0");
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("synthetic", config));
            context.getBeanFactory().registerSingleton("dataSource", dataSource);
            org.apache.ibatis.session.Configuration mapperConfig = new org.apache.ibatis.session.Configuration();
            mapperConfig.setEnvironment(new org.apache.ibatis.mapping.Environment("synthetic", new org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory(), dataSource));
            org.apache.ibatis.session.SqlSessionFactory sessions = new org.apache.ibatis.session.SqlSessionFactoryBuilder().build(mapperConfig);
            context.getBeanFactory().registerSingleton("sqlSessionFactory", sessions);
            context.getBeanFactory().registerSingleton("fellows", mock(SmtAdmittanceFellowMapper.class));
            context.getBeanFactory().registerSingleton("applies", mock(SmtAdmittanceApplyMapper.class));
            context.getBeanFactory().registerSingleton("images", mock(ImageService.class));
            context.getBeanFactory().registerSingleton("personnel", mock(ClientPersonnelDirectory.class));
            context.register(SupplierAccessConfiguration.class, SupplierAccessController.class);
            context.refresh();
            assertNotNull(context.getBean(SupplierAccessController.class));
            assertTrue(context.getBean(SupplierAccessRepository.class) instanceof JdbcSupplierAccessRepository);
            assertEquals("0", context.getBean(SupplierAccessProperties.class).getPosts().get(0).getAdmittanceAreaTypeCode());
            verifyZeroInteractions(dataSource);
        }
    }
}
