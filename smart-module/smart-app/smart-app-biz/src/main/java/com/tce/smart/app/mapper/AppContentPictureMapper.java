package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppContentPicture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:47
 */
@Mapper
public interface AppContentPictureMapper extends BaseMapper<AppContentPicture> {
  //void addBootPage(@Param("pic") AppContentPicture appContentPicture);
}
