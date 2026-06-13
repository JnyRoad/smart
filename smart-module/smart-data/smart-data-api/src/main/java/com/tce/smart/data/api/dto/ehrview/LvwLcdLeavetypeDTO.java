package com.tce.smart.data.api.dto.ehrview;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public class LvwLcdLeavetypeDTO implements Serializable {

    private static final long serialVersionUID = -2689223046344480772L;

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
	public String toString() {
		return "LvwLcdLeavetype [id=" + id + ", title=" + title + ", remark=" + remark + ", xunit=" + xunit + "]";
	}


}
