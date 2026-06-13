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
public class EstaffRegisterReqDTO extends BaseAO {

    private static final long serialVersionUID = -1826113232363908749L;

    private Integer id;
    private Integer SeqID;
    private Integer Type;
    private Integer ezid;
    private Boolean isCyear;
    private Double cyearAdjust;
    private Integer OldEID;
    private String OldBadge;
    private String Badge;
    private String Name;
    private String eName;
    private Integer CompID;
    private Integer DepID;
    private String JobID;
    private Integer reportto;
    private Integer WFReportTo;
    private Integer EmpType;
    private Integer EmpGrade;
    private Integer EmpCategory;
    private Integer EmpProperty;
    private Integer EmpGroup;
    private Integer EmpKind;
    private Integer JoinType;
    private Integer WorkCity;
    private Integer Status;
    private Date JoinDate;
    private Boolean isPrac;
    private Integer PracTerm;
    private Date PracEndDate;
    private Boolean isProb;
    private Integer ProbTerm;
    private Date ProbEndDate;
    private Integer contract;
    private Integer conType;
    private Integer conProperty;
    private String conNo;
    private Date conBeginDate;
    private Integer conTerm;
    private Date conEndDate;
    private Integer Country;
    private Integer CertType;
    private String CertNo;
    private Date birthday;
    private Integer Gender;
    private Date workbegindate;
    private Integer Regby;
    private Date RegDate;
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
    private Integer Rappid;
    private Date JobBeginTime;
    private Integer EmpAuType;
    private Integer PostLevel;
    private Integer PostGrade;
    private Integer DisEmptype;
    private Integer DisCompany;
    private Date LabEndTime;
    private Integer AssignmentCity;
    private Date AssignmentEndTime;
    private Integer TalentSave;
    private Integer depOne;
    private Integer depTwo;
    private Integer DepThree;
    private Integer JQunID;
    private Integer JZuID;
    private Integer JZongID;
    private Integer JchenID;
    private Integer JXianID;
    private Integer Jobgrade;
    private Integer Jobtype;
    private String flcj;
    private Boolean IsThrough;
    private Date ValidDate;
    private String EmergencyName;
    private Integer Relation;
    private String Telephone;
    private Boolean IsProcess;
    private Date effectdate;
    private Double tradeyearAdjust;
    private Date tradebegindate;
    private String residentaddress;
    private Integer nation;
    private Integer salarytype;
    private Integer eTjtypte;
    private Date eTjdate;
    private Date eTjenddate;
    private String phone;
    private String email;
    private Integer EmpJGkind;
    private Boolean Iscg;
    private String police;
    private Date validdatefm;
    private String PerSonEmail;
    private String PANNum;
    private String Aadhar;
    private String VoterNum;
    private String DrivingNo;
    private Date Validity;
    private String bankNo;
    private String ifsc;
    private String WedDate;
    private String blood;
    private String PreAddress;
    private String pqbadge;
    private Integer marriage;
    private Integer Incorp;
    private byte[]  EmpPhoto;


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

    public Integer getType() {
        return Type;
    }

    public void setType(Integer Type) {
        this.Type = Type;
    }

    public Integer getEzid() {
        return ezid;
    }

    public void setEzid(Integer ezid) {
        this.ezid = ezid;
    }

    public Boolean getCyear() {
        return isCyear;
    }

    public void setCyear(Boolean isCyear) {
        this.isCyear = isCyear;
    }

    public Double getCyearAdjust() {
        return cyearAdjust;
    }

    public void setCyearAdjust(Double cyearAdjust) {
        this.cyearAdjust = cyearAdjust;
    }

    public Integer getOldEID() {
        return OldEID;
    }

    public void setOldEID(Integer OldEID) {
        this.OldEID = OldEID;
    }

    public String getOldBadge() {
        return OldBadge;
    }

    public void setOldBadge(String OldBadge) {
        this.OldBadge = OldBadge;
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

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public Integer getCompID() {
        return CompID;
    }

    public void setCompID(Integer CompID) {
        this.CompID = CompID;
    }

    public Integer getDepID() {
        return DepID;
    }

    public void setDepID(Integer DepID) {
        this.DepID = DepID;
    }

    public String getJobID() {
        return JobID;
    }

    public void setJobID(String JobID) {
        this.JobID = JobID;
    }

    public Integer getReportto() {
        return reportto;
    }

    public void setReportto(Integer reportto) {
        this.reportto = reportto;
    }

    public Integer getWFReportTo() {
        return WFReportTo;
    }

    public void setWFReportTo(Integer WFReportTo) {
        this.WFReportTo = WFReportTo;
    }

    public Integer getEmpType() {
        return EmpType;
    }

    public void setEmpType(Integer EmpType) {
        this.EmpType = EmpType;
    }

    public Integer getEmpGrade() {
        return EmpGrade;
    }

    public void setEmpGrade(Integer EmpGrade) {
        this.EmpGrade = EmpGrade;
    }

    public Integer getEmpCategory() {
        return EmpCategory;
    }

    public void setEmpCategory(Integer EmpCategory) {
        this.EmpCategory = EmpCategory;
    }

    public Integer getEmpProperty() {
        return EmpProperty;
    }

    public void setEmpProperty(Integer EmpProperty) {
        this.EmpProperty = EmpProperty;
    }

    public Integer getEmpGroup() {
        return EmpGroup;
    }

    public void setEmpGroup(Integer EmpGroup) {
        this.EmpGroup = EmpGroup;
    }

    public Integer getEmpKind() {
        return EmpKind;
    }

    public void setEmpKind(Integer EmpKind) {
        this.EmpKind = EmpKind;
    }

    public Integer getJoinType() {
        return JoinType;
    }

    public void setJoinType(Integer JoinType) {
        this.JoinType = JoinType;
    }

    public Integer getWorkCity() {
        return WorkCity;
    }

    public void setWorkCity(Integer WorkCity) {
        this.WorkCity = WorkCity;
    }

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer Status) {
        this.Status = Status;
    }

    public Date getJoinDate() {
        return JoinDate;
    }

    public void setJoinDate(Date JoinDate) {
        this.JoinDate = JoinDate;
    }

    public Boolean getPrac() {
        return isPrac;
    }

    public void setPrac(Boolean isPrac) {
        this.isPrac = isPrac;
    }

    public Integer getPracTerm() {
        return PracTerm;
    }

    public void setPracTerm(Integer PracTerm) {
        this.PracTerm = PracTerm;
    }

    public Date getPracEndDate() {
        return PracEndDate;
    }

    public void setPracEndDate(Date PracEndDate) {
        this.PracEndDate = PracEndDate;
    }

    public Boolean getProb() {
        return isProb;
    }

    public void setProb(Boolean isProb) {
        this.isProb = isProb;
    }

    public Integer getProbTerm() {
        return ProbTerm;
    }

    public void setProbTerm(Integer ProbTerm) {
        this.ProbTerm = ProbTerm;
    }

    public Date getProbEndDate() {
        return ProbEndDate;
    }

    public void setProbEndDate(Date ProbEndDate) {
        this.ProbEndDate = ProbEndDate;
    }

    public Integer getContract() {
        return contract;
    }

    public void setContract(Integer contract) {
        this.contract = contract;
    }

    public Integer getConType() {
        return conType;
    }

    public void setConType(Integer conType) {
        this.conType = conType;
    }

    public Integer getConProperty() {
        return conProperty;
    }

    public void setConProperty(Integer conProperty) {
        this.conProperty = conProperty;
    }

    public String getConNo() {
        return conNo;
    }

    public void setConNo(String conNo) {
        this.conNo = conNo;
    }

    public Date getConBeginDate() {
        return conBeginDate;
    }

    public void setConBeginDate(Date conBeginDate) {
        this.conBeginDate = conBeginDate;
    }

    public Integer getConTerm() {
        return conTerm;
    }

    public void setConTerm(Integer conTerm) {
        this.conTerm = conTerm;
    }

    public Date getConEndDate() {
        return conEndDate;
    }

    public void setConEndDate(Date conEndDate) {
        this.conEndDate = conEndDate;
    }

    public Integer getCountry() {
        return Country;
    }

    public void setCountry(Integer Country) {
        this.Country = Country;
    }

    public Integer getCertType() {
        return CertType;
    }

    public void setCertType(Integer CertType) {
        this.CertType = CertType;
    }

    public String getCertNo() {
        return CertNo;
    }

    public void setCertNo(String CertNo) {
        this.CertNo = CertNo;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getGender() {
        return Gender;
    }

    public void setGender(Integer Gender) {
        this.Gender = Gender;
    }

    public Date getWorkbegindate() {
        return workbegindate;
    }

    public void setWorkbegindate(Date workbegindate) {
        this.workbegindate = workbegindate;
    }

    public Integer getRegby() {
        return Regby;
    }

    public void setRegby(Integer Regby) {
        this.Regby = Regby;
    }

    public Date getRegDate() {
        return RegDate;
    }

    public void setRegDate(Date RegDate) {
        this.RegDate = RegDate;
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

    public Integer getRappid() {
        return Rappid;
    }

    public void setRappid(Integer Rappid) {
        this.Rappid = Rappid;
    }

    public Date getJobBeginTime() {
        return JobBeginTime;
    }

    public void setJobBeginTime(Date JobBeginTime) {
        this.JobBeginTime = JobBeginTime;
    }

    public Integer getEmpAuType() {
        return EmpAuType;
    }

    public void setEmpAuType(Integer EmpAuType) {
        this.EmpAuType = EmpAuType;
    }

    public Integer getPostLevel() {
        return PostLevel;
    }

    public void setPostLevel(Integer PostLevel) {
        this.PostLevel = PostLevel;
    }

    public Integer getPostGrade() {
        return PostGrade;
    }

    public void setPostGrade(Integer PostGrade) {
        this.PostGrade = PostGrade;
    }

    public Integer getDisEmptype() {
        return DisEmptype;
    }

    public void setDisEmptype(Integer DisEmptype) {
        this.DisEmptype = DisEmptype;
    }

    public Integer getDisCompany() {
        return DisCompany;
    }

    public void setDisCompany(Integer DisCompany) {
        this.DisCompany = DisCompany;
    }

    public Date getLabEndTime() {
        return LabEndTime;
    }

    public void setLabEndTime(Date LabEndTime) {
        this.LabEndTime = LabEndTime;
    }

    public Integer getAssignmentCity() {
        return AssignmentCity;
    }

    public void setAssignmentCity(Integer AssignmentCity) {
        this.AssignmentCity = AssignmentCity;
    }

    public Date getAssignmentEndTime() {
        return AssignmentEndTime;
    }

    public void setAssignmentEndTime(Date AssignmentEndTime) {
        this.AssignmentEndTime = AssignmentEndTime;
    }

    public Integer getTalentSave() {
        return TalentSave;
    }

    public void setTalentSave(Integer TalentSave) {
        this.TalentSave = TalentSave;
    }

    public Integer getDepOne() {
        return depOne;
    }

    public void setDepOne(Integer depOne) {
        this.depOne = depOne;
    }

    public Integer getDepTwo() {
        return depTwo;
    }

    public void setDepTwo(Integer depTwo) {
        this.depTwo = depTwo;
    }

    public Integer getDepThree() {
        return DepThree;
    }

    public void setDepThree(Integer DepThree) {
        this.DepThree = DepThree;
    }

    public Integer getJQunID() {
        return JQunID;
    }

    public void setJQunID(Integer JQunID) {
        this.JQunID = JQunID;
    }

    public Integer getJZuID() {
        return JZuID;
    }

    public void setJZuID(Integer JZuID) {
        this.JZuID = JZuID;
    }

    public Integer getJZongID() {
        return JZongID;
    }

    public void setJZongID(Integer JZongID) {
        this.JZongID = JZongID;
    }

    public Integer getJchenID() {
        return JchenID;
    }

    public void setJchenID(Integer JchenID) {
        this.JchenID = JchenID;
    }

    public Integer getJXianID() {
        return JXianID;
    }

    public void setJXianID(Integer JXianID) {
        this.JXianID = JXianID;
    }

    public Integer getJobgrade() {
        return Jobgrade;
    }

    public void setJobgrade(Integer Jobgrade) {
        this.Jobgrade = Jobgrade;
    }

    public Integer getJobtype() {
        return Jobtype;
    }

    public void setJobtype(Integer Jobtype) {
        this.Jobtype = Jobtype;
    }

    public String getFlcj() {
        return flcj;
    }

    public void setFlcj(String flcj) {
        this.flcj = flcj;
    }

    public Boolean getIsThrough() {
        return IsThrough;
    }

    public void setIsThrough(Boolean IsThrough) {
        this.IsThrough = IsThrough;
    }

    public Date getValidDate() {
        return ValidDate;
    }

    public void setValidDate(Date ValidDate) {
        this.ValidDate = ValidDate;
    }

    public String getEmergencyName() {
        return EmergencyName;
    }

    public void setEmergencyName(String EmergencyName) {
        this.EmergencyName = EmergencyName;
    }

    public Integer getRelation() {
        return Relation;
    }

    public void setRelation(Integer Relation) {
        this.Relation = Relation;
    }

    public String getTelephone() {
        return Telephone;
    }

    public void setTelephone(String Telephone) {
        this.Telephone = Telephone;
    }

    public Boolean getIsProcess() {
        return IsProcess;
    }

    public void setIsProcess(Boolean IsProcess) {
        this.IsProcess = IsProcess;
    }

    public Date getEffectdate() {
        return effectdate;
    }

    public void setEffectdate(Date effectdate) {
        this.effectdate = effectdate;
    }

    public Double getTradeyearAdjust() {
        return tradeyearAdjust;
    }

    public void setTradeyearAdjust(Double tradeyearAdjust) {
        this.tradeyearAdjust = tradeyearAdjust;
    }

    public Date getTradebegindate() {
        return tradebegindate;
    }

    public void setTradebegindate(Date tradebegindate) {
        this.tradebegindate = tradebegindate;
    }

    public String getResidentaddress() {
        return residentaddress;
    }

    public void setResidentaddress(String residentaddress) {
        this.residentaddress = residentaddress;
    }

    public Integer getNation() {
        return nation;
    }

    public void setNation(Integer nation) {
        this.nation = nation;
    }

    public Integer getSalarytype() {
        return salarytype;
    }

    public void setSalarytype(Integer salarytype) {
        this.salarytype = salarytype;
    }

    public Integer geteTjtypte() {
        return eTjtypte;
    }

    public void seteTjtypte(Integer eTjtypte) {
        this.eTjtypte = eTjtypte;
    }

    public Date geteTjdate() {
        return eTjdate;
    }

    public void seteTjdate(Date eTjdate) {
        this.eTjdate = eTjdate;
    }

    public Date geteTjenddate() {
        return eTjenddate;
    }

    public void seteTjenddate(Date eTjenddate) {
        this.eTjenddate = eTjenddate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getEmpJGkind() {
        return EmpJGkind;
    }

    public void setEmpJGkind(Integer EmpJGkind) {
        this.EmpJGkind = EmpJGkind;
    }

    public Boolean getIscg() {
        return Iscg;
    }

    public void setIscg(Boolean Iscg) {
        this.Iscg = Iscg;
    }

    public String getPolice() {
        return police;
    }

    public void setPolice(String police) {
        this.police = police;
    }

    public Date getValiddatefm() {
        return validdatefm;
    }

    public void setValiddatefm(Date validdatefm) {
        this.validdatefm = validdatefm;
    }

    public String getPerSonEmail() {
        return PerSonEmail;
    }

    public void setPerSonEmail(String PerSonEmail) {
        this.PerSonEmail = PerSonEmail;
    }

    public String getPANNum() {
        return PANNum;
    }

    public void setPANNum(String PANNum) {
        this.PANNum = PANNum;
    }

    public String getAadhar() {
        return Aadhar;
    }

    public void setAadhar(String Aadhar) {
        this.Aadhar = Aadhar;
    }

    public String getVoterNum() {
        return VoterNum;
    }

    public void setVoterNum(String VoterNum) {
        this.VoterNum = VoterNum;
    }

    public String getDrivingNo() {
        return DrivingNo;
    }

    public void setDrivingNo(String DrivingNo) {
        this.DrivingNo = DrivingNo;
    }

    public Date getValidity() {
        return Validity;
    }

    public void setValidity(Date Validity) {
        this.Validity = Validity;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getWedDate() {
        return WedDate;
    }

    public void setWedDate(String WedDate) {
        this.WedDate = WedDate;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getPreAddress() {
        return PreAddress;
    }

    public void setPreAddress(String PreAddress) {
        this.PreAddress = PreAddress;
    }

    public String getPqbadge() {
        return pqbadge;
    }

    public void setPqbadge(String pqbadge) {
        this.pqbadge = pqbadge;
    }

    public Integer getMarriage() {
        return marriage;
    }

    public void setMarriage(Integer marriage) {
        this.marriage = marriage;
    }

    public Integer getIncorp() {
        return Incorp;
    }

    public void setIncorp(Integer Incorp) {
        this.Incorp = Incorp;
    }


    @Override
    public String toString() {
        return "EstaffRegisterReqDTO{" +
        ", id=" + id +
        ", SeqID=" + SeqID +
        ", Type=" + Type +
        ", ezid=" + ezid +
        ", isCyear=" + isCyear +
        ", cyearAdjust=" + cyearAdjust +
        ", OldEID=" + OldEID +
        ", OldBadge=" + OldBadge +
        ", Badge=" + Badge +
        ", Name=" + Name +
        ", eName=" + eName +
        ", CompID=" + CompID +
        ", DepID=" + DepID +
        ", JobID=" + JobID +
        ", reportto=" + reportto +
        ", WFReportTo=" + WFReportTo +
        ", EmpType=" + EmpType +
        ", EmpGrade=" + EmpGrade +
        ", EmpCategory=" + EmpCategory +
        ", EmpProperty=" + EmpProperty +
        ", EmpGroup=" + EmpGroup +
        ", EmpKind=" + EmpKind +
        ", JoinType=" + JoinType +
        ", WorkCity=" + WorkCity +
        ", Status=" + Status +
        ", JoinDate=" + JoinDate +
        ", isPrac=" + isPrac +
        ", PracTerm=" + PracTerm +
        ", PracEndDate=" + PracEndDate +
        ", isProb=" + isProb +
        ", ProbTerm=" + ProbTerm +
        ", ProbEndDate=" + ProbEndDate +
        ", contract=" + contract +
        ", conType=" + conType +
        ", conProperty=" + conProperty +
        ", conNo=" + conNo +
        ", conBeginDate=" + conBeginDate +
        ", conTerm=" + conTerm +
        ", conEndDate=" + conEndDate +
        ", Country=" + Country +
        ", CertType=" + CertType +
        ", CertNo=" + CertNo +
        ", birthday=" + birthday +
        ", Gender=" + Gender +
        ", workbegindate=" + workbegindate +
        ", Regby=" + Regby +
        ", RegDate=" + RegDate +
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
        ", Rappid=" + Rappid +
        ", JobBeginTime=" + JobBeginTime +
        ", EmpAuType=" + EmpAuType +
        ", PostLevel=" + PostLevel +
        ", PostGrade=" + PostGrade +
        ", DisEmptype=" + DisEmptype +
        ", DisCompany=" + DisCompany +
        ", LabEndTime=" + LabEndTime +
        ", AssignmentCity=" + AssignmentCity +
        ", AssignmentEndTime=" + AssignmentEndTime +
        ", TalentSave=" + TalentSave +
        ", depOne=" + depOne +
        ", depTwo=" + depTwo +
        ", DepThree=" + DepThree +
        ", JQunID=" + JQunID +
        ", JZuID=" + JZuID +
        ", JZongID=" + JZongID +
        ", JchenID=" + JchenID +
        ", JXianID=" + JXianID +
        ", Jobgrade=" + Jobgrade +
        ", Jobtype=" + Jobtype +
        ", flcj=" + flcj +
        ", IsThrough=" + IsThrough +
        ", ValidDate=" + ValidDate +
        ", EmergencyName=" + EmergencyName +
        ", Relation=" + Relation +
        ", Telephone=" + Telephone +
        ", IsProcess=" + IsProcess +
        ", effectdate=" + effectdate +
        ", tradeyearAdjust=" + tradeyearAdjust +
        ", tradebegindate=" + tradebegindate +
        ", residentaddress=" + residentaddress +
        ", nation=" + nation +
        ", salarytype=" + salarytype +
        ", eTjtypte=" + eTjtypte +
        ", eTjdate=" + eTjdate +
        ", eTjenddate=" + eTjenddate +
        ", phone=" + phone +
        ", email=" + email +
        ", EmpJGkind=" + EmpJGkind +
        ", Iscg=" + Iscg +
        ", police=" + police +
        ", validdatefm=" + validdatefm +
        ", PerSonEmail=" + PerSonEmail +
        ", PANNum=" + PANNum +
        ", Aadhar=" + Aadhar +
        ", VoterNum=" + VoterNum +
        ", DrivingNo=" + DrivingNo +
        ", Validity=" + Validity +
        ", bankNo=" + bankNo +
        ", ifsc=" + ifsc +
        ", WedDate=" + WedDate +
        ", blood=" + blood +
        ", PreAddress=" + PreAddress +
        ", pqbadge=" + pqbadge +
        ", marriage=" + marriage +
        ", Incorp=" + Incorp +
        "}";
    }

	public byte[] getEmpPhoto() {
		return EmpPhoto;
	}

	public void setEmpPhoto(byte[] empPhoto) {
		EmpPhoto = empPhoto;
	}
}
