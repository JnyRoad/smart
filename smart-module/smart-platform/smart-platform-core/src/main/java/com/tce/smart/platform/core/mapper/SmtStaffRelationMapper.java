package com.tce.smart.platform.core.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtStaffRelation;

/**
 * 员工人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
public interface SmtStaffRelationMapper extends BaseMapper<SmtStaffRelation> {

    List<SmtStaffRelation> selectByStaffId(String staffId);
}
