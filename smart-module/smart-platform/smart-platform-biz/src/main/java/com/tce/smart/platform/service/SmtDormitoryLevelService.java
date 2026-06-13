package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtDormitoryLevel;

import java.util.List;

/**
 * 宿舍职层关联表
 *
 * @author 齐佩
 * @date 2019-04-18 14:47:57
 */
public interface SmtDormitoryLevelService extends IService<SmtDormitoryLevel> {

	List<SmtDormitoryLevel> getByType(Integer typeId );

	List<SmtDormitoryLevel> getByJcheId(String jcheId );

}
