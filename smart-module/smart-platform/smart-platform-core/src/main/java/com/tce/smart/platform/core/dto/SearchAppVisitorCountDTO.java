package com.tce.smart.platform.core.dto;


import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAppVisitorCountDTO extends Model<SearchAppVisitorCountDTO> {
private static final long serialVersionUID = 1L;


private String staffBadge; //员工的员工号
}
