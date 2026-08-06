package com.tce.smart.common.core.security;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** 调用方白名单仅接受 IP/CIDR，不允许 DNS 名称在运行时改变授权边界。 */
public class LegacyCompatibilityCidrTest {

	@Test
	public void matchesOnlyAddressesInsideTheConfiguredIpv4Range() {
		LegacyCompatibilityCidr cidr = LegacyCompatibilityCidr.parse("10.13.21.0/24");

		assertTrue(cidr.matches("10.13.21.30"));
		assertFalse(cidr.matches("10.13.22.30"));
	}

	@Test
	public void rejectsDnsNamesInsteadOfResolvingThemAsSourceAddresses() {
		try {
			LegacyCompatibilityCidr.parse("doorlock.example/24");
		} catch (IllegalArgumentException expected) {
			return;
		}
		throw new AssertionError("来源白名单不得接受 DNS 名称");
	}

	@Test
	public void rejectsAbbreviatedIpv4AndIpv4MappedIpv6InsteadOfNormalizingThem() {
		assertRejected("127.1/24");
		assertRejected("::ffff:10.13.21.30/32");
	}

	@Test
	public void matchesReturnsFalseForMalformedOrDnsLikeSourceValues() {
		LegacyCompatibilityCidr cidr = LegacyCompatibilityCidr.parse("10.13.21.0/24");

		assertFalse(cidr.matches("127.1"));
		assertFalse(cidr.matches("dead"));
		assertFalse(cidr.matches("face"));
	}

	@Test
	public void matchesIpv6OnlyWhenTheAddressFamilyAndPrefixMatch() {
		LegacyCompatibilityCidr cidr = LegacyCompatibilityCidr.parse("2001:db8:7::/64");

		assertTrue(cidr.matches("2001:db8:7::30"));
		assertFalse(cidr.matches("2001:db8:8::30"));
	}

	private void assertRejected(String value) {
		try {
			LegacyCompatibilityCidr.parse(value);
		} catch (IllegalArgumentException expected) {
			return;
		}
		throw new AssertionError("必须拒绝非规范 IP literal: " + value);
	}
}
