package com.tce.smart.data.api.dto.temporary.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
public class EleaveJjitemReqDTO extends BaseAO {

    private static final long serialVersionUID = 7164821762301635724L;

    private Integer id;
    @NotNull(message = "人事区域不可空")
    private Integer ezid;
    @NotBlank(message = "员工号不可空")
    private String badge;
    @NotBlank(message = "员工姓名不可空")
    private String name;
//    @NotNull(message = "入职时间不可空")
//    @TableField("StartDate")
//    private Date startDate;
    @NotNull(message = "离职时间不可空")
    private Date leaveDate;
    @NotNull(message = "责任部门不可空")
    private Integer zrDep;
    @NotBlank(message = "交接项不可空")
    private String jjItem;
    @NotBlank(message = "交接人工号不可空")
    private String jjr;
    @NotBlank(message = "交接人姓名不可空")
    private String jjrName;
    @NotBlank(message = "确认人工号不可空")
    private String qrr;
    @NotBlank(message = "确认人姓名不可空")
    private String qrrName;
    private Double je;
    private String jjRemark;
    @NotNull(message = "开始标识不可空")
    private Integer jjBegin;
    @NotNull(message = "开始时间不可空")
    private Date jjBegintime;
    @NotNull(message = "结束标识不可空")
    private Integer jjClosed;
    @NotNull(message = "结束时间不可空")
    private Date jjClosedTime;
    @TableField("EID")
    private Integer eid;

    public EleaveJjitemReqDTO() {
        super();
    }

    public EleaveJjitemReqDTO(Integer ezid, String badge,
							  String name, Date leaveDate,
							  Integer zrDep, String jjItem,
							  String jjr, String jjrName,
							  String qrr, String qrrName, Double je,
							  String jjRemark, Integer jjBegin,
							  Date jjBegintime, Integer jjClosed,
							  Date jjClosedTime, Integer eid) {
        this.ezid = ezid;
        this.badge = badge;
        this.name = name;
        this.leaveDate = leaveDate;
        this.zrDep = zrDep;
        this.jjItem = jjItem;
        this.jjr = jjr;
        this.jjrName = jjrName;
        this.qrr = qrr;
        this.qrrName = qrrName;
        this.je = je;
        this.jjRemark = jjRemark;
        this.jjBegin = jjBegin;
        this.jjBegintime = jjBegintime;
        this.jjClosed = jjClosed;
        this.jjClosedTime = jjClosedTime;
        this.eid = eid;
    }


}
