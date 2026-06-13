package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 补卡查询参数
 *
 * @author 梁圆
 * @date 2019-05-08 18:18:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchPatchDTO extends Model<SearchPatchDTO> {
private static final long serialVersionUID = 1L;

    /**
   * 开始时间
   */
    private String patchDate;
    /**
     * 员工号
     */
    private String staffBadge;
}
