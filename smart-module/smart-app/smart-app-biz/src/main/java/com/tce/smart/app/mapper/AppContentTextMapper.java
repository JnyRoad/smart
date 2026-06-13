package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppContentText;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文本内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:49:29
 */
@Mapper
public interface AppContentTextMapper extends BaseMapper<AppContentText> {

    //boolean addContentText(AppContentText appContentText);
}
