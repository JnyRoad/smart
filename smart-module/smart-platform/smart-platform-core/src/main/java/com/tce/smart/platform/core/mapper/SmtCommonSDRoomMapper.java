package com.tce.smart.platform.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.commonsd.CommonSDRecordDTO;
import com.tce.smart.platform.core.entity.SmtCommonSD;
import com.tce.smart.platform.core.entity.SmtCommonSDRoom;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * 公摊水电关联房间
 *
 */
public interface SmtCommonSDRoomMapper extends BaseMapper<SmtCommonSDRoom> {

}
