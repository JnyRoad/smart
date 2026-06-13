package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppSubjectContentPicture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 主题图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:02
 */
@Mapper
public interface AppSubjectContentPictureMapper extends BaseMapper<AppSubjectContentPicture> {
  void addBootPicSubject(@Param("app") AppSubjectContentPicture appSubjectContentPicture);
}
