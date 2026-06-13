package com.tce.smart.app.wrapper;

import com.tce.smart.app.entity.AppVersionControl;
import com.tce.smart.app.vo.AppVersionControlVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName: ChannelListWrapper
 * @Package com.tce.operator.jsiot.admin.wrapper
 * @Description:
 * @Author wuxinjian
 * @Date 2018/12/25 9:33
 * @Version V1.0
 */
@Component
public class AppVersionControlWrapper extends BaseWrapper<AppVersionControl, AppVersionControlVo> {

    @Override
    protected AppVersionControlVo warp(AppVersionControl appVersionControl) throws IOException {
        AppVersionControlVo vo = new AppVersionControlVo();
        BeanUtils.copyProperties(appVersionControl, vo);
        //TODO 如有关联字段或者其他字段时在这里写
        return vo;
    }
}
