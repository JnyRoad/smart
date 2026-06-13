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
@TableName("aCardLost_Register")
public class AcardlostRegister extends Model<AcardlostRegister> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;
    @TableField("EZID")
    private Integer ezid;
    private Date Term;
    @TableField("EID")
    private Integer eid;
    private String Badge;
    private String Name;
    private Integer compid;
    private Integer DepID;
    private String jobid;
    @TableField("TWID")
    private Integer twid;
    private Date KqStartDate;
    private String KqInTime2;
    private String KqInTime4;
    private String KqInTime5;
    private String KqOutTime2;
    private String KqOutTime4;
    private String KqOutTime5;
    private String Remarks;
    private Date DataCreateDay;
    private String ErrMsg;
    private Boolean IsDisPose;
    private Integer Unit;
    private Integer Reason;
    private Integer RegBy;
    private Date Regdate;
    private Boolean Initialized;
    private Integer InitializedBy;
    private Date InitializedTime;
    private Boolean Submit;
    private Integer SubmitBy;
    private Date SubmitTime;
    private Boolean Closed;
    private Integer ClosedBy;
    private Date ClosedTime;
    private Integer SeqID;
    private String KqBillNo;
    private String EmpNoList;
    private String shift;
    private String stdIn2;
    private String stdOt2;
    private String stdIn4;
    private String stdOt4;
    private String stdIn5;
    private String stdOt5;
    private Boolean out2;
    private Boolean in4;
    private Boolean out4;
    private Boolean in5;
    private Boolean out5;
    @TableField("FJ")
    private String fj;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEzid() {
        return ezid;
    }

    public void setEzid(Integer ezid) {
        this.ezid = ezid;
    }

    public Date getTerm() {
        return Term;
    }

    public void setTerm(Date Term) {
        this.Term = Term;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public String getBadge() {
        return Badge;
    }

    public void setBadge(String Badge) {
        this.Badge = Badge;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Integer getCompid() {
        return compid;
    }

    public void setCompid(Integer compid) {
        this.compid = compid;
    }

    public Integer getDepID() {
        return DepID;
    }

    public void setDepID(Integer DepID) {
        this.DepID = DepID;
    }

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public Integer getTwid() {
        return twid;
    }

    public void setTwid(Integer twid) {
        this.twid = twid;
    }

    public Date getKqStartDate() {
        return KqStartDate;
    }

    public void setKqStartDate(Date KqStartDate) {
        this.KqStartDate = KqStartDate;
    }

    public String getKqInTime2() {
        return KqInTime2;
    }

    public void setKqInTime2(String KqInTime2) {
        this.KqInTime2 = KqInTime2;
    }

    public String getKqInTime4() {
        return KqInTime4;
    }

    public void setKqInTime4(String KqInTime4) {
        this.KqInTime4 = KqInTime4;
    }

    public String getKqInTime5() {
        return KqInTime5;
    }

    public void setKqInTime5(String KqInTime5) {
        this.KqInTime5 = KqInTime5;
    }

    public String getKqOutTime2() {
        return KqOutTime2;
    }

    public void setKqOutTime2(String KqOutTime2) {
        this.KqOutTime2 = KqOutTime2;
    }

    public String getKqOutTime4() {
        return KqOutTime4;
    }

    public void setKqOutTime4(String KqOutTime4) {
        this.KqOutTime4 = KqOutTime4;
    }

    public String getKqOutTime5() {
        return KqOutTime5;
    }

    public void setKqOutTime5(String KqOutTime5) {
        this.KqOutTime5 = KqOutTime5;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String Remarks) {
        this.Remarks = Remarks;
    }

    public Date getDataCreateDay() {
        return DataCreateDay;
    }

    public void setDataCreateDay(Date DataCreateDay) {
        this.DataCreateDay = DataCreateDay;
    }

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String ErrMsg) {
        this.ErrMsg = ErrMsg;
    }

    public Boolean getIsDisPose() {
        return IsDisPose;
    }

    public void setIsDisPose(Boolean IsDisPose) {
        this.IsDisPose = IsDisPose;
    }

    public Integer getUnit() {
        return Unit;
    }

    public void setUnit(Integer Unit) {
        this.Unit = Unit;
    }

    public Integer getReason() {
        return Reason;
    }

    public void setReason(Integer Reason) {
        this.Reason = Reason;
    }

    public Integer getRegBy() {
        return RegBy;
    }

    public void setRegBy(Integer RegBy) {
        this.RegBy = RegBy;
    }

    public Date getRegdate() {
        return Regdate;
    }

    public void setRegdate(Date Regdate) {
        this.Regdate = Regdate;
    }

    public Boolean getInitialized() {
        return Initialized;
    }

    public void setInitialized(Boolean Initialized) {
        this.Initialized = Initialized;
    }

    public Integer getInitializedBy() {
        return InitializedBy;
    }

    public void setInitializedBy(Integer InitializedBy) {
        this.InitializedBy = InitializedBy;
    }

    public Date getInitializedTime() {
        return InitializedTime;
    }

    public void setInitializedTime(Date InitializedTime) {
        this.InitializedTime = InitializedTime;
    }

    public Boolean getSubmit() {
        return Submit;
    }

    public void setSubmit(Boolean Submit) {
        this.Submit = Submit;
    }

    public Integer getSubmitBy() {
        return SubmitBy;
    }

    public void setSubmitBy(Integer SubmitBy) {
        this.SubmitBy = SubmitBy;
    }

    public Date getSubmitTime() {
        return SubmitTime;
    }

    public void setSubmitTime(Date SubmitTime) {
        this.SubmitTime = SubmitTime;
    }

    public Boolean getClosed() {
        return Closed;
    }

    public void setClosed(Boolean Closed) {
        this.Closed = Closed;
    }

    public Integer getClosedBy() {
        return ClosedBy;
    }

    public void setClosedBy(Integer ClosedBy) {
        this.ClosedBy = ClosedBy;
    }

    public Date getClosedTime() {
        return ClosedTime;
    }

    public void setClosedTime(Date ClosedTime) {
        this.ClosedTime = ClosedTime;
    }

    public Integer getSeqID() {
        return SeqID;
    }

    public void setSeqID(Integer SeqID) {
        this.SeqID = SeqID;
    }

    public String getKqBillNo() {
        return KqBillNo;
    }

    public void setKqBillNo(String KqBillNo) {
        this.KqBillNo = KqBillNo;
    }

    public String getEmpNoList() {
        return EmpNoList;
    }

    public void setEmpNoList(String EmpNoList) {
        this.EmpNoList = EmpNoList;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getStdIn2() {
        return stdIn2;
    }

    public void setStdIn2(String stdIn2) {
        this.stdIn2 = stdIn2;
    }

    public String getStdOt2() {
        return stdOt2;
    }

    public void setStdOt2(String stdOt2) {
        this.stdOt2 = stdOt2;
    }

    public String getStdIn4() {
        return stdIn4;
    }

    public void setStdIn4(String stdIn4) {
        this.stdIn4 = stdIn4;
    }

    public String getStdOt4() {
        return stdOt4;
    }

    public void setStdOt4(String stdOt4) {
        this.stdOt4 = stdOt4;
    }

    public String getStdIn5() {
        return stdIn5;
    }

    public void setStdIn5(String stdIn5) {
        this.stdIn5 = stdIn5;
    }

    public String getStdOt5() {
        return stdOt5;
    }

    public void setStdOt5(String stdOt5) {
        this.stdOt5 = stdOt5;
    }

    public Boolean getOut2() {
        return out2;
    }

    public void setOut2(Boolean out2) {
        this.out2 = out2;
    }

    public Boolean getIn4() {
        return in4;
    }

    public void setIn4(Boolean in4) {
        this.in4 = in4;
    }

    public Boolean getOut4() {
        return out4;
    }

    public void setOut4(Boolean out4) {
        this.out4 = out4;
    }

    public Boolean getIn5() {
        return in5;
    }

    public void setIn5(Boolean in5) {
        this.in5 = in5;
    }

    public Boolean getOut5() {
        return out5;
    }

    public void setOut5(Boolean out5) {
        this.out5 = out5;
    }

    public String getFj() {
        return fj;
    }

    public void setFj(String fj) {
        this.fj = fj;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "AcardlostRegister{" +
        ", id=" + id +
        ", ezid=" + ezid +
        ", Term=" + Term +
        ", eid=" + eid +
        ", Badge=" + Badge +
        ", Name=" + Name +
        ", compid=" + compid +
        ", DepID=" + DepID +
        ", jobid=" + jobid +
        ", twid=" + twid +
        ", KqStartDate=" + KqStartDate +
        ", KqInTime2=" + KqInTime2 +
        ", KqInTime4=" + KqInTime4 +
        ", KqInTime5=" + KqInTime5 +
        ", KqOutTime2=" + KqOutTime2 +
        ", KqOutTime4=" + KqOutTime4 +
        ", KqOutTime5=" + KqOutTime5 +
        ", Remarks=" + Remarks +
        ", DataCreateDay=" + DataCreateDay +
        ", ErrMsg=" + ErrMsg +
        ", IsDisPose=" + IsDisPose +
        ", Unit=" + Unit +
        ", Reason=" + Reason +
        ", RegBy=" + RegBy +
        ", Regdate=" + Regdate +
        ", Initialized=" + Initialized +
        ", InitializedBy=" + InitializedBy +
        ", InitializedTime=" + InitializedTime +
        ", Submit=" + Submit +
        ", SubmitBy=" + SubmitBy +
        ", SubmitTime=" + SubmitTime +
        ", Closed=" + Closed +
        ", ClosedBy=" + ClosedBy +
        ", ClosedTime=" + ClosedTime +
        ", SeqID=" + SeqID +
        ", KqBillNo=" + KqBillNo +
        ", EmpNoList=" + EmpNoList +
        ", shift=" + shift +
        ", stdIn2=" + stdIn2 +
        ", stdOt2=" + stdOt2 +
        ", stdIn4=" + stdIn4 +
        ", stdOt4=" + stdOt4 +
        ", stdIn5=" + stdIn5 +
        ", stdOt5=" + stdOt5 +
        ", out2=" + out2 +
        ", in4=" + in4 +
        ", out4=" + out4 +
        ", in5=" + in5 +
        ", out5=" + out5 +
        ", fj=" + fj +
        "}";
    }
}
