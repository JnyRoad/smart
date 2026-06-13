package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwEapprais;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface IEvwEappraisService extends IService<EvwEapprais> {

    /**
     * 根据员工工号查询评优记录
     * @param badge 员工工号
     * @return
     */
	List<EvwEapprais> getListByBadge(String badge);
}
