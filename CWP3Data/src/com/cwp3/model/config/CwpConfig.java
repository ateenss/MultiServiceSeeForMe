package com.cwp3.model.config;

/**
 * Created by csw on 2017/4/19 20:04.
 * Explain: CWP算法相关的配置参数
 * 用于单船
 */
public class CwpConfig {

    private Long berthId; //靠泊Id

    //全局参数
    private Long oneCntTime; //所有桥机平均效率，25关/小时
    private Integer twinWeightDiff; //双箱吊工艺的重量差允许范围
    private Double safeDistance; //桥机安全距离，14米
    private Long crossBarTime;//桥机跨机械起趴大梁移动时间，900s
    private Double craneMoveSpeed; //桥机移动速度，0.75m/s
    private Long unlockTwistTime; //甲板上拆锁时间，甲板五层高及以上集装箱拆锁用时，90s
    private Long hatchCoverTime; //桥机作业单块舱盖板时间，240s
    private Long single20Time; //单20尺普通箱作业用时，120s
    private Long single20FootPadTime; //单20尺垫脚箱作业用时，180s
    private Long single20SeparateTime; //单20尺全隔槽作业用时，180s
    private Long single20HalfSeparateTime; //单20尺半隔槽作业用时，180s
    private Long single40Time; //单40尺普通箱作业用时，120s
    private Long single45Time; //单45尺普通箱作业用时，120s
    private Long double20Time; //双20尺普通箱作业用时，150s
    private Long double40Time; //双吊具40尺作业用时，140s
    private Long double45Time; //双吊具45尺作业用时，140s
    private Long specialCntTime; //超限箱、分体大件作业用时，360s
    private Long dangerCntTime; //直装直提危险品作业用时，360s
    private Long badCntTime; //故障箱处理用时，360s
    private Long hatchScanTime; //桥机换倍船扫时间，300s
    private Double impactFactor; //特殊因素影响效率的影响因子，1.0

    //单船功能参数
    private Boolean setupBridge;//是否过驾驶台
    private Boolean setupChimney;//是否果烟囱
    private String ldStrategy;//装卸策略，即边装边卸：BLD、一般装卸：LD，默认驳船不做边装边卸，大船能做边装边卸就做
    private Integer craneAdviceNumber;//建议开路数

    //单船CWP决策参数
    private String loadPrior; //船舶开路装卸平衡考虑参数。首次开路全装、首次开路全卸、首次开路装卸错开:"L"、"D"、"LD"
    private String craneSameWorkTime; //均衡每部桥吊的作业量，整船桥吊同时完工，Y/N
    private String deckWorkLater; //避免开工作业甲板装船箱，避让时间为开工后一小时，Y/N
    private String mainRoadOneCrane; //重点路单桥吊持续作业，其余箱量由左右桥吊分配， Y/N
    private String dividedHatchFirst; //分割舱优先作业设定，Y/N

    public Long getHatchScanTime() {
        return hatchScanTime;
    }

    public void setHatchScanTime(Long hatchScanTime) {
        this.hatchScanTime = hatchScanTime;
    }

    public Long getOneCntTime() {
        return oneCntTime;
    }

    public void setOneCntTime(Long oneCntTime) {
        this.oneCntTime = oneCntTime;
    }

    public Integer getTwinWeightDiff() {
        return twinWeightDiff;
    }

    public void setTwinWeightDiff(Integer twinWeightDiff) {
        this.twinWeightDiff = twinWeightDiff;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Double getSafeDistance() {
        return safeDistance;
    }

    public void setSafeDistance(Double safeDistance) {
        this.safeDistance = safeDistance;
    }

    public Long getCrossBarTime() {
        return crossBarTime;
    }

    public void setCrossBarTime(Long crossBarTime) {
        this.crossBarTime = crossBarTime;
    }

    public Double getCraneMoveSpeed() {
        return craneMoveSpeed;
    }

    public void setCraneMoveSpeed(Double craneMoveSpeed) {
        this.craneMoveSpeed = craneMoveSpeed;
    }

    public Long getUnlockTwistTime() {
        return unlockTwistTime;
    }

    public void setUnlockTwistTime(Long unlockTwistTime) {
        this.unlockTwistTime = unlockTwistTime;
    }

    public Long getHatchCoverTime() {
        return hatchCoverTime;
    }

    public void setHatchCoverTime(Long hatchCoverTime) {
        this.hatchCoverTime = hatchCoverTime;
    }

    public Long getSingle20Time() {
        return single20Time;
    }

    public void setSingle20Time(Long single20Time) {
        this.single20Time = single20Time;
    }

    public Long getSingle20FootPadTime() {
        return single20FootPadTime;
    }

    public void setSingle20FootPadTime(Long single20FootPadTime) {
        this.single20FootPadTime = single20FootPadTime;
    }

    public Long getSingle20SeparateTime() {
        return single20SeparateTime;
    }

    public void setSingle20SeparateTime(Long single20SeparateTime) {
        this.single20SeparateTime = single20SeparateTime;
    }

    public Long getSingle20HalfSeparateTime() {
        return single20HalfSeparateTime;
    }

    public void setSingle20HalfSeparateTime(Long single20HalfSeparateTime) {
        this.single20HalfSeparateTime = single20HalfSeparateTime;
    }

    public Long getSingle40Time() {
        return single40Time;
    }

    public void setSingle40Time(Long single40Time) {
        this.single40Time = single40Time;
    }

    public Long getSingle45Time() {
        return single45Time;
    }

    public void setSingle45Time(Long single45Time) {
        this.single45Time = single45Time;
    }

    public Long getDouble20Time() {
        return double20Time;
    }

    public void setDouble20Time(Long double20Time) {
        this.double20Time = double20Time;
    }

    public Long getDouble40Time() {
        return double40Time;
    }

    public void setDouble40Time(Long double40Time) {
        this.double40Time = double40Time;
    }

    public Long getDouble45Time() {
        return double45Time;
    }

    public void setDouble45Time(Long double45Time) {
        this.double45Time = double45Time;
    }

    public Long getSpecialCntTime() {
        return specialCntTime;
    }

    public void setSpecialCntTime(Long specialCntTime) {
        this.specialCntTime = specialCntTime;
    }

    public Long getDangerCntTime() {
        return dangerCntTime;
    }

    public void setDangerCntTime(Long dangerCntTime) {
        this.dangerCntTime = dangerCntTime;
    }

    public Long getBadCntTime() {
        return badCntTime;
    }

    public void setBadCntTime(Long badCntTime) {
        this.badCntTime = badCntTime;
    }

    public Double getImpactFactor() {
        return impactFactor;
    }

    public void setImpactFactor(Double impactFactor) {
        this.impactFactor = impactFactor;
    }

    public Boolean getSetupBridge() {
        return setupBridge;
    }

    public void setSetupBridge(Boolean setupBridge) {
        this.setupBridge = setupBridge;
    }

    public Boolean getSetupChimney() {
        return setupChimney;
    }

    public void setSetupChimney(Boolean setupChimney) {
        this.setupChimney = setupChimney;
    }

    public String getLdStrategy() {
        return ldStrategy;
    }

    public void setLdStrategy(String ldStrategy) {
        this.ldStrategy = ldStrategy;
    }

    public Integer getCraneAdviceNumber() {
        return craneAdviceNumber;
    }

    public void setCraneAdviceNumber(Integer craneAdviceNumber) {
        this.craneAdviceNumber = craneAdviceNumber;
    }

    public String getLoadPrior() {
        return loadPrior;
    }

    public void setLoadPrior(String loadPrior) {
        this.loadPrior = loadPrior;
    }

    public String getCraneSameWorkTime() {
        return craneSameWorkTime;
    }

    public void setCraneSameWorkTime(String craneSameWorkTime) {
        this.craneSameWorkTime = craneSameWorkTime;
    }

    public String getDeckWorkLater() {
        return deckWorkLater;
    }

    public void setDeckWorkLater(String deckWorkLater) {
        this.deckWorkLater = deckWorkLater;
    }

    public String getMainRoadOneCrane() {
        return mainRoadOneCrane;
    }

    public void setMainRoadOneCrane(String mainRoadOneCrane) {
        this.mainRoadOneCrane = mainRoadOneCrane;
    }

    public String getDividedHatchFirst() {
        return dividedHatchFirst;
    }

    public void setDividedHatchFirst(String dividedHatchFirst) {
        this.dividedHatchFirst = dividedHatchFirst;
    }
}
