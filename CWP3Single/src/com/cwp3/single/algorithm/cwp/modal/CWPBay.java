package com.cwp3.single.algorithm.cwp.modal;


import com.cwp3.domain.CWPDomain;

/**
 * Created by csw on 2017/9/19.
 * Description: 倍位作业信息(一般分三个倍位置作业)
 */
public class CWPBay {

    private Integer bayNo; //倍位号
    private Long hatchId; //舱Id
    private Double workPosition; //倍位中心位置，即桥机在该倍位作业位置
    private String bayType; //驾驶台当作一个舱，三个倍位看待

    //DP过程中，倍位作业量动态信息
    private Long dpTotalWorkTime; //该倍位作业时间总量，初始化时的量
    private Long dpCurrentTotalWorkTime; //该倍位当前时刻，剩余作业时间总量
    private Long dpAvailableWorkTime; //该倍位当前时刻，可以作业的时间量

    //分析倍位特征
    private Boolean dpSteppingCntFlag; //判断可作业量是全部的垫脚箱，则为true
    private Long dpAvailableDiscWt; //卸船可作业量
    private Long dpAvailableLoadWt; //装船可作业量


    public CWPBay(Integer bayNo, Long hatchId, Double workPosition) {
        this.bayNo = bayNo;
        this.hatchId = hatchId;
        this.workPosition = workPosition;
        bayType = CWPDomain.BAY_TYPE_NATURAL;
        dpTotalWorkTime = 0L;
        dpCurrentTotalWorkTime = 0L;
        dpAvailableWorkTime = 0L;
        dpSteppingCntFlag = Boolean.FALSE;
    }

    public Integer getBayNo() {
        return bayNo;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public Double getWorkPosition() {
        return workPosition;
    }

    public String getBayType() {
        return bayType;
    }

    public void setBayType(String bayType) {
        this.bayType = bayType;
    }

    public Long getDpTotalWorkTime() {
        return dpTotalWorkTime;
    }

    public void setDpTotalWorkTime(Long dpTotalWorkTime) {
        this.dpTotalWorkTime = dpTotalWorkTime;
    }

    public void addDpTotalWorkTime(Long dpTotalWorkTime) {
        this.dpTotalWorkTime += dpTotalWorkTime;
    }

    public Long getDpCurrentTotalWorkTime() {
        return dpCurrentTotalWorkTime;
    }

    public void setDpCurrentTotalWorkTime(Long dpCurrentTotalWorkTime) {
        this.dpCurrentTotalWorkTime = dpCurrentTotalWorkTime;
    }

    public Long getDpAvailableWorkTime() {
        return dpAvailableWorkTime;
    }

    public void setDpAvailableWorkTime(Long dpAvailableWorkTime) {
        this.dpAvailableWorkTime = dpAvailableWorkTime;
    }

    public Boolean getDpSteppingCntFlag() {
        return dpSteppingCntFlag;
    }

    public void setDpSteppingCntFlag(Boolean dpSteppingCntFlag) {
        this.dpSteppingCntFlag = dpSteppingCntFlag;
    }

    public Long getDpAvailableDiscWt() {
        return dpAvailableDiscWt;
    }

    public void setDpAvailableDiscWt(Long dpAvailableDiscWt) {
        this.dpAvailableDiscWt = dpAvailableDiscWt;
    }

    public Long getDpAvailableLoadWt() {
        return dpAvailableLoadWt;
    }

    public void setDpAvailableLoadWt(Long dpAvailableLoadWt) {
        this.dpAvailableLoadWt = dpAvailableLoadWt;
    }

}