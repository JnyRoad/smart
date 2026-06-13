package com.tce.smart.ehrview.core.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@TableName("ovw_oCD_jobcustom2")
public class OvwOcdJobcustom2 extends Model<OvwOcdJobcustom2> {

    private static final long serialVersionUID = 1L;

    @TableField("ID")
    private Integer id;
    private String code;
    private String title;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "OvwOcdJobcustom2{" +
        ", id=" + id +
        ", code=" + code +
        ", title=" + title +
        "}";
    }
}
