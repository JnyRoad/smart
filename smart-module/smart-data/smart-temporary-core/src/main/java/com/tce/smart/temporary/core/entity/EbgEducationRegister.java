package com.tce.smart.temporary.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("eBG_Education_register")
public class EbgEducationRegister extends Model<EbgEducationRegister> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;
    @TableField("SeqID")
    private Integer SeqID;
    @TableField("Begindate")
    private Date BeginDate;
    @TableField("Enddate")
    private Date endDate;
    @TableField("SCHOOLNAME")
    private String SchoolName;
    @TableField("GRADTYPE")
    private Integer GradType;
    @TableField("STUDYTYPE")
    private Integer StudyType;
    @TableField("EDUTYPE")
    private Integer EduType;
    @TableField("DEGREETYPE")
    private Integer DegreeType;
    @TableField("DEGREENAME")
    private String DegreeName;
    @TableField("MAJOR")
    private String Major;
	@TableField("EduNo")
    private String EduNo;
	@TableField("EduNoDate")
    private Date EduNoDate;
	@TableField("DEGREENO")
    private String DegreeNo;
	@TableField("DEGREENODATE")
    private Date DegreeNoDate;
	@TableField("SCHOOLPLACE")
    private String SchoolPlace;
	@TableField("Reference")
    private String Reference;
	@TableField("Tel")
    private String Tel;
	@TableField("isout")
    private Boolean isout;
	@TableField("remark")
    private String remark;
	@TableField("ISHIGHEDUTYPE")
    private Boolean IsHighEdutype;
	@TableField("ISHIGHDEGREETYPE")
    private Boolean IsHighDegreeType;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSeqID() {
        return SeqID;
    }

    public void setSeqID(Integer SeqID) {
        this.SeqID = SeqID;
    }

    public Date getBeginDate() {
        return BeginDate;
    }

    public void setBeginDate(Date BeginDate) {
        this.BeginDate = BeginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSchoolName() {
        return SchoolName;
    }

    public void setSchoolName(String SchoolName) {
        this.SchoolName = SchoolName;
    }

    public Integer getGradType() {
        return GradType;
    }

    public void setGradType(Integer GradType) {
        this.GradType = GradType;
    }

    public Integer getStudyType() {
        return StudyType;
    }

    public void setStudyType(Integer StudyType) {
        this.StudyType = StudyType;
    }

    public Integer getEduType() {
        return EduType;
    }

    public void setEduType(Integer EduType) {
        this.EduType = EduType;
    }

    public Integer getDegreeType() {
        return DegreeType;
    }

    public void setDegreeType(Integer DegreeType) {
        this.DegreeType = DegreeType;
    }

    public String getDegreeName() {
        return DegreeName;
    }

    public void setDegreeName(String DegreeName) {
        this.DegreeName = DegreeName;
    }

    public String getMajor() {
        return Major;
    }

    public void setMajor(String Major) {
        this.Major = Major;
    }

    public String getEduNo() {
        return EduNo;
    }

    public void setEduNo(String EduNo) {
        this.EduNo = EduNo;
    }

    public Date getEduNoDate() {
        return EduNoDate;
    }

    public void setEduNoDate(Date EduNoDate) {
        this.EduNoDate = EduNoDate;
    }

    public String getDegreeNo() {
        return DegreeNo;
    }

    public void setDegreeNo(String DegreeNo) {
        this.DegreeNo = DegreeNo;
    }

    public Date getDegreeNoDate() {
        return DegreeNoDate;
    }

    public void setDegreeNoDate(Date DegreeNoDate) {
        this.DegreeNoDate = DegreeNoDate;
    }

    public String getSchoolPlace() {
        return SchoolPlace;
    }

    public void setSchoolPlace(String SchoolPlace) {
        this.SchoolPlace = SchoolPlace;
    }

    public String getReference() {
        return Reference;
    }

    public void setReference(String Reference) {
        this.Reference = Reference;
    }

    public String getTel() {
        return Tel;
    }

    public void setTel(String Tel) {
        this.Tel = Tel;
    }

    public Boolean getIsout() {
        return isout;
    }

    public void setIsout(Boolean isout) {
        this.isout = isout;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getIsHighEdutype() {
        return IsHighEdutype;
    }

    public void setIsHighEdutype(Boolean IsHighEdutype) {
        this.IsHighEdutype = IsHighEdutype;
    }

    public Boolean getIsHighDegreeType() {
        return IsHighDegreeType;
    }

    public void setIsHighDegreeType(Boolean IsHighDegreeType) {
        this.IsHighDegreeType = IsHighDegreeType;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "EbgEducationRegister{" +
        ", id=" + id +
        ", SeqID=" + SeqID +
        ", BeginDate=" + BeginDate +
        ", endDate=" + endDate +
        ", SchoolName=" + SchoolName +
        ", GradType=" + GradType +
        ", StudyType=" + StudyType +
        ", EduType=" + EduType +
        ", DegreeType=" + DegreeType +
        ", DegreeName=" + DegreeName +
        ", Major=" + Major +
        ", EduNo=" + EduNo +
        ", EduNoDate=" + EduNoDate +
        ", DegreeNo=" + DegreeNo +
        ", DegreeNoDate=" + DegreeNoDate +
        ", SchoolPlace=" + SchoolPlace +
        ", Reference=" + Reference +
        ", Tel=" + Tel +
        ", isout=" + isout +
        ", remark=" + remark +
        ", IsHighEdutype=" + IsHighEdutype +
        ", IsHighDegreeType=" + IsHighDegreeType +
        "}";
    }
}
