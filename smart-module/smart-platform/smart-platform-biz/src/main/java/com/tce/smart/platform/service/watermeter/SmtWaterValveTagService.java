package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveTagAddDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterValveTag;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:31
 */
public interface SmtWaterValveTagService extends IService<SmtWaterValveTag> {

	/**
	 * 通过tagId获取标签名称
	 * @param valveId
	 * @return
	 */
	List<SmtDeviceTag> getTagByValveId(Long valveId);
	/**
	 * 新增/修改水表外置阀门标签
	 * @param dto
	 * @return
	 */
	Boolean setValveTag(WaterValveTagAddDTO dto);
}
