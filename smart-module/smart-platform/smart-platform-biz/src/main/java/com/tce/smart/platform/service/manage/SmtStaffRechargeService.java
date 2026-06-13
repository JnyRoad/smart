package com.tce.smart.platform.service.manage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.manage.RechargePageReqDTO;
import com.tce.smart.platform.core.entity.manage.SmtStaffRecharge;
import com.tce.smart.platform.core.vo.RechargePageVO;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
public interface SmtStaffRechargeService extends IService<SmtStaffRecharge> {

	/**
	 * 分页返回员工充值名单数据
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	IPage<RechargePageVO> getPage(Page page, RechargePageReqDTO reqDTO);

	/**
	 * 同步新员工重置名单
	 */
	Boolean syncNewStaff();

	/**
	 * 请求在职员工名单
	 * @return
	 */
	Boolean syncSeniorRecharge();

	/**
	 * 同步充值名单到C6
	 * @return
	 */
	String syncToC6(RechargePageReqDTO req);

	/**
	 * 生成流水号
	 * @return
	 */
	String genSerialNumber();

	/**
	 * 特殊名单充值
	 * @param badges
	 * @param remark
	 */
	String saveSingleRecharge(String badges, String remark);

	/**
	 * 删除充值名单
	 * @param reqDTO
	 * @return
	 */
	Boolean deleteInfo(RechargePageReqDTO reqDTO);


	/**
	 * 修改餐补
	 * @param account
	 * @return
	 */
	Boolean updateRecharge(BigDecimal account, String remark, String id);

}
