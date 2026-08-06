package com.tce.smart.platform.service.energy;

import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterScopeRule;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/** 总分表规则必须优先避免重复累计。 */
public class EnergyScopeDecisionTest {
	@Test
	public void childIsExcludedWhenIncludedParentExists() {
		SmtEnergyMeterScopeRule parent = rule(1L, null, 1, 9L);
		SmtEnergyMeterScopeRule child = rule(2L, 1L, 1, 9L);
		assertEquals("EXCLUDED", EnergyScopeDecision.decide(child, Arrays.asList(child, parent)).getDecision());
	}
	@Test
	public void ambiguousGroupIsInvalid() {
		SmtEnergyMeterScopeRule one = rule(1L, null, 1, 9L);
		SmtEnergyMeterScopeRule two = rule(2L, null, 1, 9L);
		assertEquals("INVALID", EnergyScopeDecision.decide(one, Arrays.asList(one, two)).getDecision());
	}
	@Test
	public void cycleWinsOverIncludedAncestor() {
		SmtEnergyMeterScopeRule one = rule(1L, 2L, 1, 9L);
		SmtEnergyMeterScopeRule two = rule(2L, 1L, 1, 9L);
		assertEquals("INVALID", EnergyScopeDecision.decide(one, Arrays.asList(one, two)).getDecision());
	}
	@Test
	public void cycleWinsOverExplicitExclusion() {
		SmtEnergyMeterScopeRule one = rule(1L, 2L, 0, 9L);
		SmtEnergyMeterScopeRule two = rule(2L, 1L, 1, 9L);
		assertEquals("INVALID", EnergyScopeDecision.decide(one, Arrays.asList(one, two)).getDecision());
	}
	private SmtEnergyMeterScopeRule rule(Long id, Long parent, int include, Long group) {
		return SmtEnergyMeterScopeRule.builder().id(id).meterId(id).parentMeterId(parent).includeFlag(include).meterGroupId(group).ruleVersion(1).build();
	}
}
