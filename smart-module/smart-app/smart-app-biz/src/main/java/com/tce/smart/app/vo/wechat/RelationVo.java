package com.tce.smart.app.vo.wechat;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Builder;
import lombok.Data;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/15 .
 * @Modified By:
 */
@Data
@Builder
public class RelationVo extends BaseVO {
    private Integer relationType; //紧急联系人关系
    private String relationTypeDesc; //紧急联系人描述
    private String emergencyName;   //紧急联系人名称
    private String emergencyPhone; //紧急联系人电话

}
