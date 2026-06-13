package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppSubjectContentText;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 主题文本内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:24
 */
@Mapper
public interface AppSubjectContentTextMapper extends BaseMapper<AppSubjectContentText> {

    boolean addSubjectContentText(AppSubjectContentText appSubjectContentText);

    Integer getTextById(@Param("query")Integer subjectId);
}
