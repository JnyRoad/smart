package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@TableName("evw_oCD_DepCost")
public class EvwOcdDepcost extends Model<EvwOcdDepcost> {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String code;
    private String Title;


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
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "EvwOcdDepcost{" +
        ", id=" + id +
        ", code=" + code +
        ", Title=" + Title +
        "}";
    }
}
