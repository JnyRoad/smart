package com.tce.smart.platform.service.energy;

import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterScopeRule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 总分表范围规则决策，遇到无法唯一判定的组关系时宁可不纳入。 */
@Getter
@AllArgsConstructor
public class EnergyScopeDecision {
	private final String decision;
	private final String reason;
	private final SmtEnergyMeterScopeRule rule;

	public boolean isIncluded() { return "INCLUDED".equals(decision) || "DEFAULT_INCLUDED".equals(decision); }

	public static EnergyScopeDecision decide(SmtEnergyMeterScopeRule current, List<SmtEnergyMeterScopeRule> candidates) {
		if (current == null) return new EnergyScopeDecision("DEFAULT_INCLUDED", "未配置范围规则，默认纳入", null);
		Map<Long, SmtEnergyMeterScopeRule> rules = newestByMeter(candidates);
		Set<Long> visited = new HashSet<>(); SmtEnergyMeterScopeRule cursor = current;
		while (cursor != null) { if (!visited.add(cursor.getMeterId())) return new EnergyScopeDecision("INVALID", "总分表父级关系存在环", current); cursor = cursor.getParentMeterId() == null ? null : rules.get(cursor.getParentMeterId()); }
		if (Integer.valueOf(0).equals(current.getIncludeFlag())) return new EnergyScopeDecision("EXCLUDED", current.getReason(), current);
		if (hasAmbiguousRoots(current, rules.values())) return new EnergyScopeDecision("INVALID", "同一计量组存在多个纳入根表，无法安全去重", current);
		cursor = current;
		while (cursor.getParentMeterId() != null) { SmtEnergyMeterScopeRule parent = rules.get(cursor.getParentMeterId()); if (parent == null) break; if (Integer.valueOf(1).equals(parent.getIncludeFlag())) return new EnergyScopeDecision("EXCLUDED", "已被父/总表 " + parent.getMeterId() + " 覆盖", current); cursor = parent; }
		return new EnergyScopeDecision("INCLUDED", current.getReason(), current);
	}

	private static Map<Long, SmtEnergyMeterScopeRule> newestByMeter(List<SmtEnergyMeterScopeRule> candidates) {
		Map<Long, SmtEnergyMeterScopeRule> result = new HashMap<>();
		if (candidates == null) return result;
		for (SmtEnergyMeterScopeRule rule : candidates) if (!result.containsKey(rule.getMeterId())) result.put(rule.getMeterId(), rule);
		return result;
	}

	private static boolean hasAmbiguousRoots(SmtEnergyMeterScopeRule current, Iterable<SmtEnergyMeterScopeRule> rules) {
		if (current.getMeterGroupId() == null) return false;
		int roots = 0;
		for (SmtEnergyMeterScopeRule rule : rules) if (current.getMeterGroupId().equals(rule.getMeterGroupId()) && rule.getParentMeterId() == null && Integer.valueOf(1).equals(rule.getIncludeFlag()) && ++roots > 1) return true;
		return false;
	}
}
