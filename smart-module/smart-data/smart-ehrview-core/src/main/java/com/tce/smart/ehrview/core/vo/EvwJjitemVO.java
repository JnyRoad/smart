package com.tce.smart.ehrview.core.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
@TableName("evw_JJitem")
public class EvwJjitemVO extends BaseVO {

    private static final long serialVersionUID = 1L;
    private Integer ezid;
    private String empzone;
    private Integer zrdep;
    private String zrdepName;
    private String jjItem;
    private String jjr;
    private String jjrName;

}
