package com.cwp3.model.vessel;

import java.util.Date;

/**
 * Created by csw on 2017/4/20 11:36.
 * Explain:
 */
public class VMContainer {

    private Long vpcCntId; //箱指令唯一编号(指令模块专用)

    private String vLocation; //船箱位
    private String type; //箱型，决定了高度，以及其他一些属性
    private String size; //箱尺寸
    private String dlType; //装卸标识
    private String containerNo; //箱号，可空
    private Long groupId;       //属性组
    private Long weightId;       //重量组
    private Long cntWorkTime; //人工设置的箱子作业效率，时间（s）

    private String throughFlag; //过境箱标记
    private String rfFlag;	//冷藏箱标记
    private String efFlag; //empty or full，箱空重
    private String dgCd; //危险品代码
    private String isHeight; // 是否高箱
    private String cntHeight; //箱子的具体高度
    private String overrunCd; //超限代码
    private Double weightKg; //重量（kg）

    private String assistWork;//附加作业：过高架，平板，钢丝绳
    private String cwoManualWorkflow; //人工指定作业工艺
    private String cwoManualSeqno; //人工指定作业顺序
    private String cwoManualWi; //人工锁定的船箱位，发箱时不能作业的箱子，CWP计划排到最后面

    private String craneNo; //桥机号
    private String workFlow; //作业工艺
    private Long moveOrder; //作业顺序
    private Date workingStartTime; //计划开始作业时间
    private Date workingEndTime; //计划结束作业时间
    private Integer moveWorkTime; //每一个move的作业时间(单位是：秒)
    private Double cranePosition;//桥机当前位置
    private Integer qdc; //是否启动舱
    private Long berthId; //靠泊Id
    private Long hatchId; //箱子所在舱
    private String directCntFlag; //直装箱标记，Y表示箱子标记为直装箱，N或者null表示非直装箱子

    public VMContainer(String vLocation, String dlType) {
        this.vLocation = vLocation;
        this.dlType = dlType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VMContainer) {
            VMContainer vmContainer = (VMContainer) obj;
            return dlType.equals(vmContainer.getDlType()) && vLocation.equals(vmContainer.getvLocation());
        } else {
            return false;
        }
    }

    public Long getVpcCntId() {
        return vpcCntId;
    }

    public void setVpcCntId(Long vpcCntId) {
        this.vpcCntId = vpcCntId;
    }

    public String getvLocation() {
        return vLocation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDlType() {
        return dlType;
    }

    public void setDlType(String dlType) {
        this.dlType = dlType;
    }

    public String getContainerNo() {
        return containerNo;
    }

    public void setContainerNo(String containerNo) {
        this.containerNo = containerNo;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getWeightId() {
        return weightId;
    }

    public void setWeightId(Long weightId) {
        this.weightId = weightId;
    }

    public Long getCntWorkTime() {
        return cntWorkTime;
    }

    public void setCntWorkTime(Long cntWorkTime) {
        this.cntWorkTime = cntWorkTime;
    }

    public String getThroughFlag() {
        return throughFlag;
    }

    public void setThroughFlag(String throughFlag) {
        this.throughFlag = throughFlag;
    }

    public String getRfFlag() {
        return rfFlag;
    }

    public void setRfFlag(String rfFlag) {
        this.rfFlag = rfFlag;
    }

    public String getEfFlag() {
        return efFlag;
    }

    public void setEfFlag(String efFlag) {
        this.efFlag = efFlag;
    }

    public String getDgCd() {
        return dgCd;
    }

    public void setDgCd(String dgCd) {
        this.dgCd = dgCd;
    }

    public String getIsHeight() {
        return isHeight;
    }

    public void setIsHeight(String isHeight) {
        this.isHeight = isHeight;
    }

    public String getCntHeight() {
        return cntHeight;
    }

    public void setCntHeight(String cntHeight) {
        this.cntHeight = cntHeight;
    }

    public String getOverrunCd() {
        return overrunCd;
    }

    public void setOverrunCd(String overrunCd) {
        this.overrunCd = overrunCd;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public String getAssistWork() {
        return assistWork;
    }

    public void setAssistWork(String assistWork) {
        this.assistWork = assistWork;
    }

    public String getCwoManualWorkflow() {
        return cwoManualWorkflow;
    }

    public void setCwoManualWorkflow(String cwoManualWorkflow) {
        this.cwoManualWorkflow = cwoManualWorkflow;
    }

    public String getCwoManualSeqno() {
        return cwoManualSeqno;
    }

    public void setCwoManualSeqno(String cwoManualSeqno) {
        this.cwoManualSeqno = cwoManualSeqno;
    }

    public String getCwoManualWi() {
        return cwoManualWi;
    }

    public void setCwoManualWi(String cwoManualWi) {
        this.cwoManualWi = cwoManualWi;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public String getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
    }

    public Long getMoveOrder() {
        return moveOrder;
    }

    public void setMoveOrder(Long moveOrder) {
        this.moveOrder = moveOrder;
    }

    public Date getWorkingStartTime() {
        return workingStartTime;
    }

    public void setWorkingStartTime(Date workingStartTime) {
        this.workingStartTime = workingStartTime;
    }

    public Date getWorkingEndTime() {
        return workingEndTime;
    }

    public void setWorkingEndTime(Date workingEndTime) {
        this.workingEndTime = workingEndTime;
    }

    public Integer getMoveWorkTime() {
        return moveWorkTime;
    }

    public void setMoveWorkTime(Integer moveWorkTime) {
        this.moveWorkTime = moveWorkTime;
    }

    public Double getCranePosition() {
        return cranePosition;
    }

    public void setCranePosition(Double cranePosition) {
        this.cranePosition = cranePosition;
    }

    public Integer getQdc() {
        return qdc;
    }

    public void setQdc(Integer qdc) {
        this.qdc = qdc;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public String getDirectCntFlag() {
        return directCntFlag;
    }

    public void setDirectCntFlag(String directCntFlag) {
        this.directCntFlag = directCntFlag;
    }
}
