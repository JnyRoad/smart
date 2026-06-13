package com.tce.smart.data.api.dto.temporary.req;

import com.tce.smart.common.core.ao.BaseAO;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public class EbgEducationRegisterReqDTO extends BaseAO {

    private static final long serialVersionUID = -4709941726098723473L;

    private Integer id;
    private Integer SeqID;
    private Date BeginDate;
    private Date endDate;
    private String SchoolName;
    private Integer GradType;
    private Integer StudyType;
    private Integer EduType;
    private Integer DegreeType;
    private String DegreeName;
    private String Major;
    private String EduNo;
    private Date EduNoDate;
    private String DegreeNo;
    private Date DegreeNoDate;
    private String SchoolPlace;
    private String Reference;
    private String Tel;
    private Boolean isout;
    private String remark;
    private Boolean IsHighEdutype;
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
    public String toString() {
        return "EbgEducationRegisterReqDTO{" +
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
