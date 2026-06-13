package com.tce.smart.app.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * App版本控制
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
@Data
public class AppVersionControlVo extends BaseVO {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Integer id;
    /**
     * 版本编号
     */
    private String versionCode;
    /**
     * 版本说明
     */
    private String versionDesc;
    /**
     * 发布状态（0:待发布；1:已发布；2:已下线））
     */
    private String publishFlag;
    /**
     * 删除状态（0:删除；1:正常）
     */
    private String delFlag;
    /**
     * 是否强制更新（0:不是；1:强制更新）
     */
    private String updateslFlag;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

}
