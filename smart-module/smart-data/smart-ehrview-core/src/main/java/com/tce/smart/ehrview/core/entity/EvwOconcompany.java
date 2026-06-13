package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@TableName("evw_OCONCOMPANY")
public class EvwOconcompany extends Model<EvwOconcompany> {

    private static final long serialVersionUID = 1L;

    private Integer compid;
    private String title;


    public Integer getCompid() {
        return compid;
    }

    public void setCompid(Integer compid) {
        this.compid = compid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "EvwOconcompany{" +
        ", compid=" + compid +
        ", title=" + title +
        "}";
    }
}
