package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@TableName("lvw_adjustBasic")
public class LvwAdjustbasic extends Model<LvwAdjustbasic> {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String badge;
    private String name;
    private Date term;
    @TableField("adjustTime")
    private Double adjustTime;
    private String title;
    @TableField("IsTiaoXiu")
    private Boolean IsTiaoXiu;
    private Integer azid;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTerm() {
        return term;
    }

    public void setTerm(Date term) {
        this.term = term;
    }

    public Double getAdjustTime() {
        return adjustTime;
    }

    public void setAdjustTime(Double adjustTime) {
        this.adjustTime = adjustTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsTiaoXiu() {
        return IsTiaoXiu;
    }

    public void setIsTiaoXiu(Boolean IsTiaoXiu) {
        this.IsTiaoXiu = IsTiaoXiu;
    }

    public Integer getAzid() {
        return azid;
    }

    public void setAzid(Integer azid) {
        this.azid = azid;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "LvwAdjustbasic{" +
        ", id=" + id +
        ", badge=" + badge +
        ", name=" + name +
        ", term=" + term +
        ", adjustTime=" + adjustTime +
        ", title=" + title +
        ", IsTiaoXiu=" + IsTiaoXiu +
        ", azid=" + azid +
        "}";
    }
}
