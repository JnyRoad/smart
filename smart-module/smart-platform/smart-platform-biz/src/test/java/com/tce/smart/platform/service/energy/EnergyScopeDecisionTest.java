package com.tce.smart.platform.service.energy;

import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterScopeRule;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** 总分表规则必须优先避免重复累计。 */
public class EnergyScopeDecisionTest {
	/** 已声明总分表关系却缺少总表规则时，不得让默认纳入的总表与分表重计。 */
	@Test
	public void missingParentRuleIsInvalidAndNotIncluded() {
		SmtEnergyMeterScopeRule child = rule(2L, 1L, 1, 9L);
		EnergyScopeDecision decision = EnergyScopeDecision.decide(child, Arrays.asList(child));
		assertEquals("INVALID", decision.getDecision());
		assertFalse(decision.isIncluded());
	}

	/** 中间父表存在但祖先缺失，必须先判完整父链而非提前接受已纳入父表。 */
	@Test
	public void brokenAncestorChainIsInvalid() {
		SmtEnergyMeterScopeRule parent = rule(2L, 1L, 1, 9L);
		SmtEnergyMeterScopeRule child = rule(3L, 2L, 1, 9L);
		assertEquals("INVALID", EnergyScopeDecision.decide(child, Arrays.asList(child, parent)).getDecision());
	}

	/** 独立表未声明任何规则仍沿用历史默认纳入口径，不扩大修复影响面。 */
	@Test
	public void standaloneMeterWithoutRuleRemainsIncluded() {
		EnergyScopeDecision decision = EnergyScopeDecision.decide(null, null);
		assertEquals("DEFAULT_INCLUDED", decision.getDecision());
		assertTrue(decision.isIncluded());
	}

	/** 完整父链全部排除时，显式纳入的子表仍能作为独立统计口径。 */
	@Test
	public void childWithExcludedParentRemainsIncluded() {
		SmtEnergyMeterScopeRule parent = rule(1L, null, 0, 9L);
		SmtEnergyMeterScopeRule child = rule(2L, 1L, 1, 9L);
		assertTrue(EnergyScopeDecision.decide(child, Arrays.asList(child, parent)).isIncluded());
	}

	/** 已纳入父表覆盖子表时必须排除子表。 */
	@Test
	public void childIsExcludedWhenIncludedParentExists() {
		SmtEnergyMeterScopeRule parent = rule(1L, null, 1, 9L);
		SmtEnergyMeterScopeRule child = rule(2L, 1L, 1, 9L);
		assertEquals("EXCLUDED", EnergyScopeDecision.decide(child, Arrays.asList(child, parent)).getDecision());
	}
	/** 同组多个纳入根表无法确定去重口径。 */
	@Test
	public void ambiguousGroupIsInvalid() {
		SmtEnergyMeterScopeRule one = rule(1L, null, 1, 9L);
		SmtEnergyMeterScopeRule two = rule(2L, null, 1, 9L);
		assertEquals("INVALID", EnergyScopeDecision.decide(one, Arrays.asList(one, two)).getDecision());
	}
	/** 环路不能被纳入父表的条件掩盖。 */
	@Test
	public void cycleWinsOverIncludedAncestor() {
		SmtEnergyMeterScopeRule one = rule(1L, 2L, 1, 9L);
		SmtEnergyMeterScopeRule two = rule(2L, 1L, 1, 9L);
		assertEquals("INVALID", EnergyScopeDecision.decide(one, Arrays.asList(one, two)).getDecision());
	}
	/** 显式排除仍需验证关系图合法，避免隐藏环路配置。 */
	@Test
	public void cycleWinsOverExplicitExclusion() {
		SmtEnergyMeterScopeRule one = rule(1L, 2L, 0, 9L);
		SmtEnergyMeterScopeRule two = rule(2L, 1L, 1, 9L);
		assertEquals("INVALID", EnergyScopeDecision.decide(one, Arrays.asList(one, two)).getDecision());
	}
	/** 创建手工规则样本，预期决策由各测试独立给出。 */
	private SmtEnergyMeterScopeRule rule(Long id, Long parent, int include, Long group) {
		return SmtEnergyMeterScopeRule.builder().id(id).meterId(id).parentMeterId(parent).includeFlag(include).meterGroupId(group).ruleVersion(1).build();
	}
}
