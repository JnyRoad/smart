package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterTagAddDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterTag;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:29
 */
public interface SmtEleMeterTagService extends IService<SmtEleMeterTag> {

	/**
	 * 通过标签ID集合获取电表ID集合
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
	 * 新增/修改电表标签
	 * @param dto
	 * @return
	 */
	Boolean setMeterTag(EleMeterTagAddDTO dto);
}
