package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("lvw_lcd_leaveType")
public class LvwLcdLeavetype extends Model<LvwLcdLeavetype> {

    private static final long serialVersionUID = 1L;

    @TableField("ID")
    private Integer id;
    private String title;
    private String remark;
    private Integer xunit;



    public Integer getXunit() {
		return xunit;
	}

	public void setXunit(Integer xunit) {
		this.xunit = xunit;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

	@Override
	public String toString() {
		return "LvwLcdLeavetype [id=" + id + ", title=" + title + ", remark=" + remark + ", xunit=" + xunit + "]";
	}


}
