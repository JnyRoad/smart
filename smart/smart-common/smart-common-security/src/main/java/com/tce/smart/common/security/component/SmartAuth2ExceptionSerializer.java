package com.tce.smart.common.security.component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.security.exception.SmartAuth2Exception;
import lombok.SneakyThrows;

/**
 * OAuth2 异常格式化
 */
public class SmartAuth2ExceptionSerializer extends StdSerializer<SmartAuth2Exception> {
	public SmartAuth2ExceptionSerializer() {
		super(SmartAuth2Exception.class);
	}

	@Override
	@SneakyThrows
	public void serialize(SmartAuth2Exception value, JsonGenerator gen, SerializerProvider provider) {
		gen.writeStartObject();
		gen.writeObjectField("code", CommonConstants.FAIL);
		gen.writeStringField("msg", value.getMessage());
		gen.writeStringField("data", value.getErrorCode());
		gen.writeEndObject();
	}
}
