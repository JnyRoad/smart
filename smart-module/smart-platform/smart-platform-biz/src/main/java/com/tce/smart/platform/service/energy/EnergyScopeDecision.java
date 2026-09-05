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

	/** 返回本次决策能否纳入汇总；无规则的独立表沿用默认纳入，不修改规则。 */
	public boolean isIncluded() { return "INCLUDED".equals(decision) || "DEFAULT_INCLUDED".equals(decision); }

	/** 根据当前有效规则及候选父级计算纳入状态；断链或循环返回 INVALID，不修改输入。 */
	public static EnergyScopeDecision decide(SmtEnergyMeterScopeRule current, List<SmtEnergyMeterScopeRule> candidates) {
		if (current == null) return new EnergyScopeDecision("DEFAULT_INCLUDED", "未配置范围规则，默认纳入", null);
		// 先验证完整父链；显式声明的父级缺失时，无法证明分表没有被默认纳入的总表覆盖。
		Map<Long, SmtEnergyMeterScopeRule> rules = newestByMeter(candidates);
		Set<Long> visited = new HashSet<>();
		SmtEnergyMeterScopeRule cursor = current;
		while (cursor != null) {
			if (!visited.add(cursor.getMeterId())) {
				return new EnergyScopeDecision("INVALID", "总分表父级关系存在环", current);
			}
			Long parentId = cursor.getParentMeterId();
			if (parentId == null) break;
			cursor = rules.get(parentId);
			if (cursor == null) {
				return new EnergyScopeDecision("INVALID", "总分表父级 " + parentId + " 缺少有效范围规则", current);
			}
		}
		// 关系有效后才应用人工排除和同组去重，避免把不完整配置误判为可用结果。
		if (Integer.valueOf(0).equals(current.getIncludeFlag())) return new EnergyScopeDecision("EXCLUDED", current.getReason(), current);
		if (hasAmbiguousRoots(current, rules.values())) return new EnergyScopeDecision("INVALID", "同一计量组存在多个纳入根表，无法安全去重", current);
		// 任一纳入的祖先已经覆盖当前分表，因此分表不再贡献用量。
		cursor = current;
		while (cursor.getParentMeterId() != null) {
			SmtEnergyMeterScopeRule parent = rules.get(cursor.getParentMeterId());
			if (Integer.valueOf(1).equals(parent.getIncludeFlag())) {
				return new EnergyScopeDecision("EXCLUDED", "已被父/总表 " + parent.getMeterId() + " 覆盖", current);
			}
			cursor = parent;
		}
		return new EnergyScopeDecision("INCLUDED", current.getReason(), current);
	}

	/** 将按有效版本倒序提供的候选规则按表 ID 去重；空候选返回空映射，不修改输入。 */
	private static Map<Long, SmtEnergyMeterScopeRule> newestByMeter(List<SmtEnergyMeterScopeRule> candidates) {
		Map<Long, SmtEnergyMeterScopeRule> result = new HashMap<>();
		if (candidates == null) return result;
		for (SmtEnergyMeterScopeRule rule : candidates) if (!result.containsKey(rule.getMeterId())) result.put(rule.getMeterId(), rule);
		return result;
	}

	/** 判断当前计量组是否存在多个纳入根表；未分组时不推断总分关系，无副作用。 */
	private static boolean hasAmbiguousRoots(SmtEnergyMeterScopeRule current, Iterable<SmtEnergyMeterScopeRule> rules) {
		if (current.getMeterGroupId() == null) return false;
		int roots = 0;
		for (SmtEnergyMeterScopeRule rule : rules) if (current.getMeterGroupId().equals(rule.getMeterGroupId()) && rule.getParentMeterId() == null && Integer.valueOf(1).equals(rule.getIncludeFlag()) && ++roots > 1) return true;
		return false;
	}
}
