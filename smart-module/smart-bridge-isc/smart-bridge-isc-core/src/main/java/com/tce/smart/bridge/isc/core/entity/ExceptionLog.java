package com.tce.smart.bridge.isc.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 园区异常消息表
 * </p>
 *
 */
@Data
@TableName("e_exception_log")
@EqualsAndHashCode(callSuper = true)
public class ExceptionLog extends Model<ExceptionLog>{
    private static final long serialVersionUID = 9133048383845268221L;

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /**
     * Kafka Key
     */
    private String eventType;
    /**
     * 消息
     */
    private String message;
	/**
	 * 创建时间
	 */
	private String createTime;
	/**
	 * 更新时间
	 */
	private String updateTime;
}
