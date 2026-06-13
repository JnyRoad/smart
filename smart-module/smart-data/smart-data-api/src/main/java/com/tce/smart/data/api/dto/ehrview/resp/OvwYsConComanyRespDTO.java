package com.tce.smart.data.api.dto.ehrview.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/***
 * description: 合同签约单位 <br>
 * date: 2019/11/27 11:36 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
public class OvwYsConComanyRespDTO extends BaseVO {

    private static final long serialVersionUID = 1852431501130767957L;

    private Integer compId;

    private String compAbbr;

    private String title;

}
