package com.tce.smart.platform.client.supplier;

import com.tce.smart.platform.core.client.supplier.JdbcSupplierAccessStore;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.client.identity.ClientPersonnelDirectory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import java.time.Clock;

/** 显式开启后才装配真实平台Mapper与同一业务DataSource；不自动建表或建立连接。 */
@Configuration
@ConditionalOnProperty(prefix = "smart.client.supplier", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SupplierAccessProperties.class)
public class SupplierAccessConfiguration {
    @Bean public SupplierAccessRepository supplierAccessRepository(DataSource dataSource) {
        return new JdbcSupplierAccessRepository(new JdbcSupplierAccessStore(dataSource));
    }
    @Bean public SupplierAdmissionSource supplierAdmissionSource(SmtAdmittanceFellowMapper fellows,
            SmtAdmittanceApplyMapper applies, ImageService images, SupplierAccessProperties properties) {
        properties.validate();
        return new SupplierAdmissionSource(fellows, applies, images, properties);
    }
    @Bean public SupplierAdmissionLock supplierAdmissionLock(DataSource dataSource, SqlSessionFactory sessions) {
        return new SupplierAdmissionLock(dataSource, sessions);
    }
    @Bean public SupplierAccessService supplierAccessService(SupplierAccessProperties properties,
            SupplierAdmissionSource source, SupplierAccessRepository repository, SupplierAdmissionLock admissionLock,
            ClientPersonnelDirectory personnel) {
        return new SupplierAccessService(properties, source, repository, Clock.systemUTC(), admissionLock, personnel);
    }
}
