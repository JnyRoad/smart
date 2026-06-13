package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface IEvwEmphrYsService extends IService<EvwEmphrYs> {

    EvwEmphrYs getByBadge(String badge);

	List<EvwEmphrYs> getByCompId(Integer compId);

	IPage<EvwEmphrYs> getPage(Page page, List<Integer> compId);

	List<EvwEmphrYs> getInStaffByCompId(Integer compId);

	IPage<EvwEmphrYs> getBlack(Page page,String cerNo, String name);
}
