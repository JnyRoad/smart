package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtNoticeSwitch;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 园区通知控制开关
 *
 * @author mckaywu
 * @date 2019-11-20 10:37:43
 */
public interface SmtNoticeSwitchService extends IService<SmtNoticeSwitch> {

	/**
	 * 根据开关编码查询
	 * @param parkId 园区Id
	 * @param switchCode 开关
	 * @return boolean true-成功,false-失败
	 */
	SmtNoticeSwitch getSwitchByCode(Integer parkId,String switchCode);

	/**
	 * 批量园区保存开关(新增或修改)
	 * @param parkId 园区Id
	 * @param switchList 开关集合
	 * @return boolean true-成功,false-失败
	 */
	@Transactional(rollbackFor = Exception.class)
	Boolean batchSave(Integer parkId,List<SmtNoticeSwitch> switchList);

	/**
	 * 获取所有开关状态
	 * @param parkId 园区Id
	 * @return
	 */
	List<SmtNoticeSwitch> listInitSwitch(Integer parkId);
}
