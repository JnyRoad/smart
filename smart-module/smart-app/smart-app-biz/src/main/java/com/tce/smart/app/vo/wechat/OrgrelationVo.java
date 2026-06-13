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
public class OrgrelationVo extends BaseVO {
    private static final long serialVersionUID = -5739949234651413004L;

        private Integer orgrelationId;   //关系id，新增时为空,修改有值
        private Integer relationType;    //亲属关系@详见接口备注
        private String relationTypeDesc;    //亲属关系描述
        private String orgPersonName;   //亲属姓名
        private String orgPersonBu;
        private String orgPersonDept;
        private String orgPersonSection;
        private Integer orgPersonGender;

}
