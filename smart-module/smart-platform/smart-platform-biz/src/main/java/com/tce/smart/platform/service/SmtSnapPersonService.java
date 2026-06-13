package com.tce.smart.platform.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.IscTemperatureDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.AreaDeviceSnapRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkVisitorRespDTO;
import com.tce.smart.platform.core.dto.SaveSnapPersonDTO;
import com.tce.smart.platform.core.dto.SearchSnapPersonAccessDTO;
import com.tce.smart.platform.core.entity.SmtSnapPerson;
import com.tce.smart.platform.core.vo.SearchSmtSnapPersonVO;
import com.tce.smart.platform.core.vo.SmtSnapPersonDetailVO;

import java.util.List;

/**
 * 人员抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:30
 */
public interface SmtSnapPersonService extends IService<SmtSnapPerson> {

	SmtSnapPersonDetailVO getSnapPersonById(Integer id);

	IPage<SearchSmtSnapPersonVO> getSmtSnapPersonPage(Page page, SearchSnapPersonAccessDTO searchSnapPersonAccessDto,String snapTime);

	Result<Boolean> addSnapPerson(SaveSnapPersonDTO saveSnapPersonDTO);

	/**
	 * 今日进出访客统计
	 * @return
	 */
	ParkVisitorRespDTO getVisitorInfo(Integer parkId);

	/**
	 * 获取区域设备抓拍数据
	 * @return
	 */
	List<AreaDeviceSnapRespDTO> getAreaDeviceSnapData(Integer parkId);

	/**
	 * 合肥温度检查
	 * @param dto
	 */
	Boolean checkTemperature(List<IscTemperatureDTO> dto);
}
