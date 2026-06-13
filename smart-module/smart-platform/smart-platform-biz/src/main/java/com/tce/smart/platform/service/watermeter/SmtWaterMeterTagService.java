package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterTagAddDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterTag;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:31
 */
public interface SmtWaterMeterTagService extends IService<SmtWaterMeterTag> {

	/**
	 * 通过标签ID集合获取水表ID集合
	 * @param tagIds
	 * @return
	 */
	List<Long> getMeterIdsByTagIds(List<Long> tagIds);

	/**
	 * 通过tagId获取标签名称
	 * @param meterId
	 * @return
	 */
	List<SmtDeviceTag> getTagByMeterId(Long meterId);
	/**
	 * 新增/修改水表标签
	 * @param dto
	 * @return
	 */
	Boolean setMeterTag(WaterMeterTagAddDTO dto);
}
