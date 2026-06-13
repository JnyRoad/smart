package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 园区通知控制开关
 *
 * @author mckaywu
 * @date 2019-11-20 10:37:43
 */
@Data
@TableName("smt_notice_switch")
@EqualsAndHashCode(callSuper = true)
public class SmtNoticeSwitch extends Model<SmtNoticeSwitch> {

	private static final long serialVersionUID = 7153046575593237566L;

	public SmtNoticeSwitch(){
		super();
	}

	public SmtNoticeSwitch(String switchCode,String switchName){
		this.switchCode = switchCode;
		this.switchName = switchName;
	}
    /**
   * 主键ID
   */
    @TableId
    private Integer id;
	/**
	 * 园区编号
	 */
	private Integer parkId;
    /**
   * 开关名称
   */
    private String switchName;
    /**
   * 开关编码
   */
    private String switchCode;
	/**
	 * 提前通知时间，单位：分钟
	 */
	private Integer beforeTime;
	/**
	 * 延后通知时间，单位：分钟
	 */
	private Integer afterTime;
    /**
   * 0：关闭，1：-开启
   */
    private Integer isOn;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
