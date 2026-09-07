package com.tce.smart.platform.client.release;

import com.tce.smart.platform.client.identity.ClientIdentityService;
import com.tce.smart.platform.client.identity.ClientPersonnelDirectory;
import com.tce.smart.platform.core.client.release.JdbcConfidentialReleaseStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.time.Clock;

/** 显式开启后才连接放行专用表；不建表、不更改旧物品放行数据或历史接口。 */
@Configuration
@EnableConfigurationProperties(ReleaseAccessProperties.class)
public class ReleaseAccessConfiguration {
	@Bean public JdbcConfidentialReleaseStore clientReleaseStore(DataSource dataSource) { return new JdbcConfidentialReleaseStore(dataSource); }
	@Bean public ClientReleaseService clientReleaseService(ReleaseAccessProperties properties, JdbcConfidentialReleaseStore store,
			ClientIdentityService identities, ClientPersonnelDirectory personnel, ReleaseCardEvidenceVerifier cards) {
		return new ClientReleaseService(properties, store, identities, personnel, cards, Clock.systemUTC());
	}
}
