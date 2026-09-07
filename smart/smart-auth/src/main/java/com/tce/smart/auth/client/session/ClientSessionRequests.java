package com.tce.smart.auth.client.session;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;

/** 严格登录载荷，避免未知字段和异常对象描述被框架日志序列化。 */
final class ClientSessionRequests {
	private ClientSessionRequests() { }

	@JsonDeserialize(using = LoginDeserializer.class)
	static final class Login {
		final String staffNo;
		final String password;
		Login(String staffNo, String password) { this.staffNo = staffNo; this.password = password; }
	}

	static final class LoginDeserializer extends JsonDeserializer<Login> {
		@Override public Login deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
			JsonNode body;
			try { body = parser.readValueAsTree(); }
			catch (IOException failure) { throw new ClientSessionException(400); }
			if (body == null || !body.isObject() || body.size() != 2) throw new ClientSessionException(400);
			JsonNode staffNo = body.get("staffNo");
			JsonNode password = body.get("password");
			if (!text(staffNo) || !text(password)) throw new ClientSessionException(400);
			return new Login(staffNo.textValue(), password.textValue());
		}
		private boolean text(JsonNode value) { return value != null && value.isTextual() && value.textValue().length() <= 128; }
	}
}
