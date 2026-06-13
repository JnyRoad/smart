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
public class RelationTypeVO extends BaseVO {
    private static final long serialVersionUID = 6557951931562098094L;

        private String relationType; //关系编号Id
        private String relationTypeDesc;    //关系名称

}
