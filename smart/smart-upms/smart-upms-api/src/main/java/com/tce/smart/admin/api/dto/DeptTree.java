package com.tce.smart.admin.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @date 2018/1/20
 * 部门树
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptTree extends TreeNode {
	private String name;
}
