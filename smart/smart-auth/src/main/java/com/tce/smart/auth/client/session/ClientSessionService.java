package com.tce.smart.auth.client.session;

import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.security.exception.NotStrongPasswordException;
import com.tce.smart.common.security.service.SmartUserDetailsService;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.LinkedHashMap;
import java.util.Map;

/** 只接受显式 JSON 工号密码，认证成功后仅返回 Bearer token 与绝对过期毫秒。 */
public class ClientSessionService {
	private final SmartUserDetailsService users;
	private final ClientSessionTokenIssuer issuer;

	ClientSessionService(SmartUserDetailsService users, ClientSessionTokenIssuer issuer) {
		this.users = users; this.issuer = issuer;
	}

	public Map<String, Object> login(String staffNo, String password) {
		if (!valid(staffNo) || !valid(password)) throw new ClientSessionException(400);
		UserDetails subject;
		try {
			subject = users.authenticate(staffNo.trim(), password);
		} catch (BadCredentialsException | NotStrongPasswordException | AccountStatusException | TCEException failure) {
			throw new ClientSessionException(401);
		} catch (Exception failure) {
			throw new ClientSessionException(503);
		}
		ClientSessionToken token = issuer.issue(subject);
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("token", token.getValue());
		response.put("expiresAt", token.getExpiresAt());
		return response;
	}

	private boolean valid(String value) {
		return value != null && !value.trim().isEmpty() && value.length() <= 128 && !hasControl(value);
	}

	private boolean hasControl(String value) {
		for (int index = 0; index < value.length(); index++) if (Character.isISOControl(value.charAt(index))) return true;
		return false;
	}
}
