package com.tce.smart.tool.enums;


import cn.hutool.core.util.ObjectUtil;
import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 节点状态
 * @author Lenovo
 *
 */
public enum NodeStatusEnum {

    APPROVE("0", "批准"), // 同意
	SAVE("1", "保存"),
	SUBMIT("2", "提交"),
	RETURN("3", "退回"),
	REOPEN("4", "重新打开"),
	DELETE("5", "删除"),
	ACTIVATION("6", "激活"),
	FORWARD("7", "转发"),
	ANNOTATION("9", "批注"),
	INTERVENTION("i", "流程干预"),
	ARCHIVE("e", "强制归档"), // 结束标识
	CC("t", "抄送"),
	SUPERVISE("s", "督办"),
	APPROVER("c", "等待审批"),
	NOT_FINISHED("n", "进行中"), // 工作交接进行中
	FINISHED("f", "已完成"), // 工作交接完成
	REVOKE("g", "撤销"); // 工作交接完成


    private final String code;

    private final String desc;

    NodeStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NodeStatusEnum nodeStatus(String code){
        if(Objects.nonNull(code)){
            for(NodeStatusEnum nodeStatus : NodeStatusEnum.values()){
                if(nodeStatus.code.equals(code)){
                    return nodeStatus;
                }
            }
        }
        return null;
    }

    public static String desc(String code){
//    	if("1".equals(code) || "e".equals(code)){
//    	    return "已同意";
//    	}else{
//    	    return "审批中";
//    	}
	NodeStatusEnum nodeStatusEnum = nodeStatus(code);
	if(ObjectUtil.isNotNull(nodeStatusEnum)) {
		return nodeStatusEnum.getDesc();
	}
	return "审批中";
    }

    public static Integer changeCode(String code){
	if("1".equals(code) || "e".equals(code)){
            return LeaveApplicationStatusEnum.APPROVED.getCode();
        }else{
            return LeaveApplicationStatusEnum.PENDING.getCode();
        }
    }

    public static String code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(NodeStatusEnum nodeStatus : NodeStatusEnum.values()){
                if(nodeStatus.desc.equals(desc)){
                    return nodeStatus.code;
                }
            }
        }
        return null;
    }

    public static boolean existnodeStatus(String code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(NodeStatusEnum alarmType : NodeStatusEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
