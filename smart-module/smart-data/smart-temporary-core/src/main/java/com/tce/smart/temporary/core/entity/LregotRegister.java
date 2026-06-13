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
@TableName("lRegOt_register")
public class LregotRegister extends Model<LregotRegister> {

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
    private Date OTTerm;
    private Date BeginTime;
    private Date EndTime;
    private Double Amount;
    private Integer Unit;
    private String Reason;
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
    private String Remark;
    private Integer SeqID;
    private Integer Ot2TypeName;
    private Integer Ot4TypeName;
    private Integer Ot5TypeName;
    private String Ot2StartTime;
    private String Ot2EndTime;
    private String Ot4StartTime;
    private String Ot4EndTime;
    private String Ot5StartTime;
    private String Ot5EndTime;
    private Boolean IsDispose;
    private String ErrMsg;
    private Date DataCreateDay;
    private String Ot2IsDirectResult;
    private String Ot4IsDirectResult;
    private String Ot5IsDirectResult;
    private String sqlid;
    private Integer OTControl;
    private Double weeks;
    private Double months;
    private Double OtweekTotal;
    private Double OtMonthTotal;
    private Boolean ifempot;
    private Integer otType;
    private Boolean iscc;
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

    public Date getOTTerm() {
        return OTTerm;
    }

    public void setOTTerm(Date OTTerm) {
        this.OTTerm = OTTerm;
    }

    public Date getBeginTime() {
        return BeginTime;
    }

    public void setBeginTime(Date BeginTime) {
        this.BeginTime = BeginTime;
    }

    public Date getEndTime() {
        return EndTime;
    }

    public void setEndTime(Date EndTime) {
        this.EndTime = EndTime;
    }

    public Double getAmount() {
        return Amount;
    }

    public void setAmount(Double Amount) {
        this.Amount = Amount;
    }

    public Integer getUnit() {
        return Unit;
    }

    public void setUnit(Integer Unit) {
        this.Unit = Unit;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String Reason) {
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

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String Remark) {
        this.Remark = Remark;
    }

    public Integer getSeqID() {
        return SeqID;
    }

    public void setSeqID(Integer SeqID) {
        this.SeqID = SeqID;
    }

    public Integer getOt2TypeName() {
        return Ot2TypeName;
    }

    public void setOt2TypeName(Integer Ot2TypeName) {
        this.Ot2TypeName = Ot2TypeName;
    }

    public Integer getOt4TypeName() {
        return Ot4TypeName;
    }

    public void setOt4TypeName(Integer Ot4TypeName) {
        this.Ot4TypeName = Ot4TypeName;
    }

    public Integer getOt5TypeName() {
        return Ot5TypeName;
    }

    public void setOt5TypeName(Integer Ot5TypeName) {
        this.Ot5TypeName = Ot5TypeName;
    }

    public String getOt2StartTime() {
        return Ot2StartTime;
    }

    public void setOt2StartTime(String Ot2StartTime) {
        this.Ot2StartTime = Ot2StartTime;
    }

    public String getOt2EndTime() {
        return Ot2EndTime;
    }

    public void setOt2EndTime(String Ot2EndTime) {
        this.Ot2EndTime = Ot2EndTime;
    }

    public String getOt4StartTime() {
        return Ot4StartTime;
    }

    public void setOt4StartTime(String Ot4StartTime) {
        this.Ot4StartTime = Ot4StartTime;
    }

    public String getOt4EndTime() {
        return Ot4EndTime;
    }

    public void setOt4EndTime(String Ot4EndTime) {
        this.Ot4EndTime = Ot4EndTime;
    }

    public String getOt5StartTime() {
        return Ot5StartTime;
    }

    public void setOt5StartTime(String Ot5StartTime) {
        this.Ot5StartTime = Ot5StartTime;
    }

    public String getOt5EndTime() {
        return Ot5EndTime;
    }

    public void setOt5EndTime(String Ot5EndTime) {
        this.Ot5EndTime = Ot5EndTime;
    }

    public Boolean getIsDispose() {
        return IsDispose;
    }

    public void setIsDispose(Boolean IsDispose) {
        this.IsDispose = IsDispose;
    }

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String ErrMsg) {
        this.ErrMsg = ErrMsg;
    }

    public Date getDataCreateDay() {
        return DataCreateDay;
    }

    public void setDataCreateDay(Date DataCreateDay) {
        this.DataCreateDay = DataCreateDay;
    }

    public String getOt2IsDirectResult() {
        return Ot2IsDirectResult;
    }

    public void setOt2IsDirectResult(String Ot2IsDirectResult) {
        this.Ot2IsDirectResult = Ot2IsDirectResult;
    }

    public String getOt4IsDirectResult() {
        return Ot4IsDirectResult;
    }

    public void setOt4IsDirectResult(String Ot4IsDirectResult) {
        this.Ot4IsDirectResult = Ot4IsDirectResult;
    }

    public String getOt5IsDirectResult() {
        return Ot5IsDirectResult;
    }

    public void setOt5IsDirectResult(String Ot5IsDirectResult) {
        this.Ot5IsDirectResult = Ot5IsDirectResult;
    }

    public String getSqlid() {
        return sqlid;
    }

    public void setSqlid(String sqlid) {
        this.sqlid = sqlid;
    }

    public Integer getOTControl() {
        return OTControl;
    }

    public void setOTControl(Integer OTControl) {
        this.OTControl = OTControl;
    }

    public Double getWeeks() {
        return weeks;
    }

    public void setWeeks(Double weeks) {
        this.weeks = weeks;
    }

    public Double getMonths() {
        return months;
    }

    public void setMonths(Double months) {
        this.months = months;
    }

    public Double getOtweekTotal() {
        return OtweekTotal;
    }

    public void setOtweekTotal(Double OtweekTotal) {
        this.OtweekTotal = OtweekTotal;
    }

    public Double getOtMonthTotal() {
        return OtMonthTotal;
    }

    public void setOtMonthTotal(Double OtMonthTotal) {
        this.OtMonthTotal = OtMonthTotal;
    }

    public Boolean getIfempot() {
        return ifempot;
    }

    public void setIfempot(Boolean ifempot) {
        this.ifempot = ifempot;
    }

    public Integer getOtType() {
        return otType;
    }

    public void setOtType(Integer otType) {
        this.otType = otType;
    }

    public Boolean getIscc() {
        return iscc;
    }

    public void setIscc(Boolean iscc) {
        this.iscc = iscc;
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
        return "LregotRegister{" +
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
        ", OTTerm=" + OTTerm +
        ", BeginTime=" + BeginTime +
        ", EndTime=" + EndTime +
        ", Amount=" + Amount +
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
        ", Remark=" + Remark +
        ", SeqID=" + SeqID +
        ", Ot2TypeName=" + Ot2TypeName +
        ", Ot4TypeName=" + Ot4TypeName +
        ", Ot5TypeName=" + Ot5TypeName +
        ", Ot2StartTime=" + Ot2StartTime +
        ", Ot2EndTime=" + Ot2EndTime +
        ", Ot4StartTime=" + Ot4StartTime +
        ", Ot4EndTime=" + Ot4EndTime +
        ", Ot5StartTime=" + Ot5StartTime +
        ", Ot5EndTime=" + Ot5EndTime +
        ", IsDispose=" + IsDispose +
        ", ErrMsg=" + ErrMsg +
        ", DataCreateDay=" + DataCreateDay +
        ", Ot2IsDirectResult=" + Ot2IsDirectResult +
        ", Ot4IsDirectResult=" + Ot4IsDirectResult +
        ", Ot5IsDirectResult=" + Ot5IsDirectResult +
        ", sqlid=" + sqlid +
        ", OTControl=" + OTControl +
        ", weeks=" + weeks +
        ", months=" + months +
        ", OtweekTotal=" + OtweekTotal +
        ", OtMonthTotal=" + OtMonthTotal +
        ", ifempot=" + ifempot +
        ", otType=" + otType +
        ", iscc=" + iscc +
        ", fj=" + fj +
        "}";
    }
}
