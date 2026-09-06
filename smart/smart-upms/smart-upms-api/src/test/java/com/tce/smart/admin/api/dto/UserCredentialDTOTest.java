package com.tce.smart.admin.api.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UserCredentialDTOTest {

	private static final String USERNAME = "test-user";
	private static final String PASSWORD = "Credential8X";

	@Test
	public void serializesCredentialForInternalPostBody() throws Exception {
		UserCredentialDTO credential = credential();

		JsonNode json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(credential));

		assertEquals(USERNAME, json.get("username").asText());
		assertEquals(PASSWORD, json.get("password").asText());
	}

	@Test
	public void toStringDoesNotExposePassword() {
		UserCredentialDTO credential = credential();

		assertFalse(credential.toString().contains(PASSWORD));
	}

	private UserCredentialDTO credential() {
		UserCredentialDTO credential = new UserCredentialDTO();
		credential.setUsername(USERNAME);
		credential.setPassword(PASSWORD);
		return credential;
	}
}
