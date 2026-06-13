package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 消息模板
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:24:52
 */
@Mapper
public interface SmtMsgTemplateMapper extends BaseMapper<SmtMsgTemplate> {

	List<SmtMsgTemplate> getSmtMessageTemplate();

}
