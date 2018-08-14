package com.cwp3.ioservice.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.data.all.MachineData;
import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.ioservice.ParseDataService;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.crane.*;
import com.cwp3.model.log.Logger;
import com.cwp3.model.other.AreaContainer;
import com.cwp3.model.vessel.*;
import com.cwp3.utils.BeanCopyUtil;
import com.cwp3.utils.StringUtil;
import com.cwp3.utils.ValidatorUtil;
import com.shbtos.biz.smart.cwp.pojo.*;
import com.shbtos.biz.smart.cwp.service.SmartCwpImportData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CarloJones on 2018/3/6.
 */
public class ParseDataServiceImpl implements ParseDataService {

    private Logger logger;

    public ParseDataServiceImpl() {
        this.logger = new Logger();
    }

    @Override
    public AllRuntimeData parseAllRuntimeData(SmartCwpImportData smartCwpImportData) {
        AllRuntimeData allRuntimeData = new AllRuntimeData();

        List<VMSchedule> vmScheduleList = parseSchedule(smartCwpImportData.getSmartScheduleIdInfoList());
        for (VMSchedule vmSchedule : vmScheduleList) {
            allRuntimeData.addStructData(new StructureData(vmSchedule.getVesselCode()));
            allRuntimeData.addWorkingData(new WorkingData(vmSchedule));
        }

        parseCMCraneWithType(smartCwpImportData.getSmartCraneBaseInfoList(), allRuntimeData.getMachineData());

        parseStructData(smartCwpImportData, allRuntimeData);
        parseWorkingData(smartCwpImportData, allRuntimeData);

        allRuntimeData.setLogger(logger);
        return allRuntimeData;
    }

    private void parseStructData(SmartCwpImportData smartCwpImportData, AllRuntimeData allRuntimeData) {
        parseHatchInfo(smartCwpImportData.getSmartVpsVslHatchsInfoList(), allRuntimeData);
        parseBayInfo(smartCwpImportData.getSmartVpsVslBaysInfoList(), allRuntimeData);
        parseRowInfo(smartCwpImportData.getSmartVpsVslRowsInfoList(), allRuntimeData);
        parseLocationInfo(smartCwpImportData.getSmartVpsVslLocationsInfoList(), allRuntimeData);
        parseHatchCoverInfo(smartCwpImportData.getSmartVpsVslHatchcoversInfoList(), allRuntimeData);
        parseMachineInfo(smartCwpImportData.getSmartVesselMachinesInfoList(), allRuntimeData);
    }

    private void parseWorkingData(SmartCwpImportData smartCwpImportData, AllRuntimeData allRuntimeData) {
        parseCraneWorkFlow(smartCwpImportData.getSmartCraneWorkFlowInfoList(), allRuntimeData);
        parseParameter(smartCwpImportData.getSmartCwpParameterInfoList(), allRuntimeData);
        parseVesselContainer(smartCwpImportData.getSmartVesselContainerInfoList(), allRuntimeData);
        parseCranePool(smartCwpImportData.getSmartVesselCranePoolInfoList(), smartCwpImportData.getSmartCranePoolInfoList(), smartCwpImportData.getSmartCraneFirstWorkInfoList(), allRuntimeData);
    }

    private List<VMSchedule> parseSchedule(List<SmartScheduleIdInfo> smartScheduleIdInfoList) {
        List<VMSchedule> vmScheduleList = new ArrayList<>();
        logger.logInfo("当前运行的CWP3算法版本号为: " + CWPDefaultValue.VERSION);
        logger.logError("航次信息", ValidatorUtil.isEmpty(smartScheduleIdInfoList));
        for (SmartScheduleIdInfo smartScheduleIdInfo : smartScheduleIdInfoList) {
            Long berthId = smartScheduleIdInfo.getBerthId();
            String vesselCode = smartScheduleIdInfo.getVesselCode();
            String planBerthDirect = smartScheduleIdInfo.getPlanBerthDirect();
            String vesselType = smartScheduleIdInfo.getVesselType();
            try {
                logger.logError("航次信息-靠泊Id", ValidatorUtil.isNull(berthId));
                logger.logError("航次信息-船舶代码", ValidatorUtil.isBlank(vesselCode));
                logger.logError("航次信息-停靠方向", ValidatorUtil.isBlank(planBerthDirect));
                logger.logError("航次信息-船舶类型", ValidatorUtil.isBlank(vesselType));
                VMSchedule vmSchedule = new VMSchedule(berthId, vesselCode);
                vmSchedule.setPlanBeginWorkTime(smartScheduleIdInfo.getPlanBeginWorkTime());
                vmSchedule.setPlanEndWorkTime(smartScheduleIdInfo.getPlanEndWorkTime());
                vmSchedule.setPlanStartPst(smartScheduleIdInfo.getPlanStartPst());
                vmSchedule.setPlanEndPst(smartScheduleIdInfo.getPlanEndPst());
                vmSchedule.setSendWorkInstruction(smartScheduleIdInfo.getSendWorkInstruction());
                logger.logInfo("berthId: " + berthId + ", vesselCode: " + vesselCode + ", planBerthDirect: " + planBerthDirect + ", vesselType: " + vesselType);
                planBerthDirect = planBerthDirect.equals("L") ? CWPDomain.VES_BER_DIRECT_L : CWPDomain.VES_BER_DIRECT_R;
                vesselType = vesselType.equals("BAR") ? CWPDomain.VESSEL_TYPE_BAR : CWPDomain.VESSEL_TYPE_FCS;
                vmSchedule.setVesselType(vesselType);
                vmSchedule.setPlanBerthDirect(planBerthDirect);
                vmScheduleList.add(vmSchedule);
            } catch (Exception e) {
                logger.logError("解析航次(berthId:" + berthId + ", vesselCode:" + vesselCode + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
        return vmScheduleList;
    }

    private void parseCMCraneWithType(List<SmartCraneBaseInfo> smartCraneBaseInfoList, MachineData machineData) {
        logger.logError("桥机信息", ValidatorUtil.isEmpty(smartCraneBaseInfoList));
        for (SmartCraneBaseInfo smartCraneBaseInfo : smartCraneBaseInfoList) {
            CMCrane cmCrane = new CMCrane(smartCraneBaseInfo.getCraneNo());
            cmCrane = (CMCrane) BeanCopyUtil.copyBean(smartCraneBaseInfo, cmCrane);
            //解析QCType
            boolean matchSomeQCType = false;
            for (CMCraneType cmCraneType : machineData.getCMCraneTypes()) {
                //判断是否符合
                if (cmCraneType.getMaxWeightKg().equals(cmCrane.getCraneMaxCarryWeight())) {
                    matchSomeQCType = true;
                    //桥机类型已经存在，绑定桥机类型
                    cmCrane.setCraneTypeId(cmCraneType.getCraneTypeId());
                    break;
                }
            }
            if (!matchSomeQCType) { //桥机类型不存在
                //增加新的type
                CMCraneType cmCraneType = new CMCraneType(StringUtil.getKey("CT", cmCrane.getCraneMaxCarryWeight()), cmCrane.getCraneMaxCarryWeight());
                //默认支持所有作业工艺
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_SINGLE20);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_DUAL20);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_SINGLE40);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_DUAL40);
                cmCraneType.addSupprtPT(CWPCraneDomain.CT_QUAD20);
                machineData.addCMCraneType(cmCraneType);
                //绑定桥机类型
                cmCrane.setCraneTypeId(cmCraneType.getCraneTypeId());
            }
            machineData.addCMCrane(cmCrane);
        }
    }

    private void parseHatchInfo(List<SmartVpsVslHatchsInfo> smartVpsVslHatchsInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("舱信息", ValidatorUtil.isEmpty(smartVpsVslHatchsInfoList));
        for (SmartVpsVslHatchsInfo smartVpsVslHatchsInfo : smartVpsVslHatchsInfoList) {
            String vesselCode = smartVpsVslHatchsInfo.getVesselCode();
            Long hatchId = smartVpsVslHatchsInfo.getHatchId();
            try {
                if (allRuntimeData.getStructDataByVesselCode(vesselCode) != null) {
                    logger.logError("舱信息-舱(Id:" + hatchId + ")信息为null", ValidatorUtil.isNull(hatchId));
                    VMHatch vmHatch = new VMHatch(hatchId);
                    logger.logError("舱信息-舱(Id:" + hatchId + ")位置坐标为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchPosition()));
                    vmHatch.setHatchPosition(smartVpsVslHatchsInfo.getHatchPosition());
                    logger.logError("舱信息-舱(Id:" + hatchId + ")长度为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchLength()));
                    vmHatch.setHatchLength(smartVpsVslHatchsInfo.getHatchLength());
                    logger.logError("舱信息-舱(Id:" + hatchId + ")序号为null", ValidatorUtil.isNull(smartVpsVslHatchsInfo.getHatchSeq()));
                    vmHatch.setHatchSeq(smartVpsVslHatchsInfo.getHatchSeq());
                    allRuntimeData.getStructDataByVesselCode(vesselCode).addVMHatch(vmHatch);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")舱(Id:" + hatchId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseBayInfo(List<SmartVpsVslBaysInfo> smartVpsVslBaysInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("倍位信息", ValidatorUtil.isEmpty(smartVpsVslBaysInfoList));
        StructureData structureData;
        for (SmartVpsVslBaysInfo smartVpsVslBaysInfo : smartVpsVslBaysInfoList) {
            Long bayId = smartVpsVslBaysInfo.getBayId();
            Long hatchId = smartVpsVslBaysInfo.getHatchId();
            String aboveOrBelow = smartVpsVslBaysInfo.getDeckOrHatch();
            String vesselCode = smartVpsVslBaysInfo.getVesselCode();
            Integer bayNo = null;
            try {
                structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    logger.logError("倍位信息-倍位号", ValidatorUtil.isNull(smartVpsVslBaysInfo.getBayNo()));
                    bayNo = Integer.valueOf(smartVpsVslBaysInfo.getBayNo());
                    logger.logError("倍位信息-倍位信息中甲板上、下字段为null", ValidatorUtil.isNull(aboveOrBelow));
                    aboveOrBelow = aboveOrBelow.equals("D") ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
                    String bayKey = StringUtil.getKey(bayNo, aboveOrBelow);
                    VMBay vmBay = new VMBay(bayId, bayKey, bayNo, aboveOrBelow, hatchId);
                    structureData.addVMBay(vmBay);
                    //设置舱内bay1、bay2，数字小在前
                    VMHatch vmHatch = structureData.getVMHatchByHatchId(hatchId);
                    vmHatch.addByNo(bayNo);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")倍位(bayNo:" + bayNo + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseRowInfo(List<SmartVpsVslRowsInfo> smartVpsVslRowsInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("排信息", ValidatorUtil.isEmpty(smartVpsVslRowsInfoList));
        StructureData structureData;
        for (SmartVpsVslRowsInfo smartVpsVslRowsInfo : smartVpsVslRowsInfoList) {
            Long bayId = smartVpsVslRowsInfo.getBayId();
            String vesselCode = smartVpsVslRowsInfo.getVesselCode();
            Integer rowNo = null;
            try {
                structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    logger.logError("排信息-排号", ValidatorUtil.isNull(smartVpsVslRowsInfo.getRowNo()));
                    rowNo = Integer.valueOf(smartVpsVslRowsInfo.getRowNo());
                    VMBay vmBay = structureData.getVMBayByBayId(bayId);
                    VMRow vmRow = new VMRow(vmBay.getBayId(), vmBay.getBayKey(), rowNo);
                    vmBay.addVMRow(vmRow);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")排(rowNo:" + rowNo + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseLocationInfo(List<SmartVpsVslLocationsInfo> smartVpsVslLocationsInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("船箱位信息", ValidatorUtil.isEmpty(smartVpsVslLocationsInfoList));
        StructureData structureData;
        for (SmartVpsVslLocationsInfo smartVpsVslLocationsInfo : smartVpsVslLocationsInfoList) {
            String vLocation = smartVpsVslLocationsInfo.getLocation();
            Long bayId = smartVpsVslLocationsInfo.getBayId();
            String vesselCode = smartVpsVslLocationsInfo.getVesselCode();
            String size = smartVpsVslLocationsInfo.getSize();
            try {
                structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    VMPosition vmPosition = new VMPosition(vLocation);
                    VMBay vmBay = structureData.getVMBayByBayId(bayId);
                    VMContainerSlot vmContainerSlot = new VMContainerSlot(vmPosition, vmBay, size);
                    structureData.addVMSlot(vmContainerSlot);
                    //要根据船箱位信息，初始化该倍位下每排的最大层号和最小层号
                    Integer rowNo = vmPosition.getRowNo();
                    VMRow vmRow = vmBay.getVMRowByRowNo(rowNo);
                    logger.logError("船箱位信息-查找不到排(" + rowNo + ")信息！", ValidatorUtil.isNull(vmRow));
                    vmRow.addVMSlot(vmContainerSlot);
                }
            } catch (Exception e) {
                logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")船箱位(vLocation:" + vLocation + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseHatchCoverInfo(List<SmartVpsVslHatchcoversInfo> smartVpsVslHatchCoversInfoList, AllRuntimeData allRuntimeData) {
        logger.logInfo("舱盖板信息", ValidatorUtil.isEmpty(smartVpsVslHatchCoversInfoList));
        StructureData structureData;
        for (SmartVpsVslHatchcoversInfo smartVpsVslHatchcoversInfo : smartVpsVslHatchCoversInfoList) {
            String vesselCode = smartVpsVslHatchcoversInfo.getVesselCode();
            structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
            if (structureData != null) {
                Long hatchCoverId = smartVpsVslHatchcoversInfo.getHatchCoverId();
                try {
                    VMHatchCover vmHatchCover = new VMHatchCover();
                    vmHatchCover.setHatchCoverNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getHatchCoverNo()));
                    vmHatchCover.setOpenSeq(smartVpsVslHatchcoversInfo.getOpenSeq());
                    vmHatchCover.setHatchFromRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getHatchFromRowNo()));
                    vmHatchCover.setHatchToRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getHatchToRowNo()));
                    vmHatchCover.setDeckFromRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getDeckFromRowNo()));
                    vmHatchCover.setDeckToRowNo(Integer.valueOf(smartVpsVslHatchcoversInfo.getDeckToRowNo()));
                    vmHatchCover.setLeftCoverFather(smartVpsVslHatchcoversInfo.getLeftCoverFather());
                    vmHatchCover.setRightCoverFather(smartVpsVslHatchcoversInfo.getRightCoverFather());
                    vmHatchCover.setFrontCoverFather(smartVpsVslHatchcoversInfo.getFrontCoverFather());
                    vmHatchCover.setBehindCoverFather(smartVpsVslHatchcoversInfo.getBehiendCoverFather());
                    //舱盖板bayNo、hatchId
                    VMBay vmBayFrom = structureData.getVMBayByBayId(smartVpsVslHatchcoversInfo.getBayIdFrom());
                    VMBay vmBayTo = structureData.getVMBayByBayId(smartVpsVslHatchcoversInfo.getBayIdTo());
                    vmHatchCover.setBayNoFrom(vmBayFrom.getBayNo());
                    vmHatchCover.setBayNoTo(vmBayTo.getBayNo());
                    vmHatchCover.setHatchId(vmBayFrom.getHatchId());
                    //VMHatchCoverSlot
                    VMPosition vmPosition = new VMPosition((vmBayFrom.getBayNo() + vmBayTo.getBayNo()) / 2, vmHatchCover.getHatchCoverNo(), 50);
                    vmHatchCover.setvLocation(vmPosition.getVLocation());
                    VMHatchCoverSlot vmHatchCoverSlot = new VMHatchCoverSlot(vmPosition, vmHatchCover.getHatchId());
                    structureData.addVMSlot(vmHatchCoverSlot);
                    structureData.addVMHatchCover(vmHatchCover);
                } catch (Exception e) {
                    logger.logError("解析船舶结构(vesselCode:" + vesselCode + ")舱盖板(Id:" + hatchCoverId + ")信息过程中发生数据异常！");
                    e.printStackTrace();
                }
            }
        }

    }

    private void parseMachineInfo(List<SmartVesselMachinesInfo> smartVesselMachinesInfoList, AllRuntimeData allRuntimeData) {
        for (SmartVesselMachinesInfo smartVesselMachinesInfo : smartVesselMachinesInfoList) {
            String vesselCode = smartVesselMachinesInfo.getVesselCode();
            Double machinePosition = smartVesselMachinesInfo.getMachinePosition();
            String machineType = smartVesselMachinesInfo.getMachineType();
            try {
                StructureData structureData = allRuntimeData.getStructDataByVesselCode(vesselCode);
                if (structureData != null) {
                    logger.logError("船舶机械(machineType:" + machineType + ")-位置坐标为null", ValidatorUtil.isNull(machinePosition));
                    VMMachine vmMachine = new VMMachine();
                    vmMachine = (VMMachine) BeanCopyUtil.copyBean(smartVesselMachinesInfo, vmMachine);
                    structureData.addVMMachine(vmMachine);
                }
            } catch (Exception e) {
                logger.logError("解析船舶机械(vesselCode:" + vesselCode + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseCraneWorkFlow(List<SmartCraneWorkFlowInfo> smartCraneWorkFlowInfoList, AllRuntimeData allRuntimeData) {
        logger.logInfo("舱作业工艺设置", ValidatorUtil.isEmpty(smartCraneWorkFlowInfoList));
        WorkingData workingData;
        for (SmartCraneWorkFlowInfo smartCraneWorkFlowInfo : smartCraneWorkFlowInfoList) {
            Long berthId = smartCraneWorkFlowInfo.getBerthId();
            Long hatchId = smartCraneWorkFlowInfo.getHatchId();
            String ldStrategy = smartCraneWorkFlowInfo.getLdStrategy();
            String aboveOrBelow = smartCraneWorkFlowInfo.getDeckOrHatch();
            ldStrategy = ldStrategy != null ? ldStrategy.equals("LD") ? CWPDomain.LD_STRATEGY_LD : CWPDomain.LD_STRATEGY_BLD : CWPDomain.LD_STRATEGY_BLD;
            try {
                aboveOrBelow = aboveOrBelow.equals("D") ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
                workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    CMCraneWorkFlow cmCraneWorkFlow = new CMCraneWorkFlow(hatchId, aboveOrBelow, CWPDomain.DL_TYPE_DISC);
                    cmCraneWorkFlow.setSingle(smartCraneWorkFlowInfo.getSingle());
                    cmCraneWorkFlow.setTwin(smartCraneWorkFlowInfo.getTwin());
                    cmCraneWorkFlow.setTandem(smartCraneWorkFlowInfo.getTandem());
                    cmCraneWorkFlow.setLdStrategy(ldStrategy);
                    workingData.addCMCraneWorkFlow(cmCraneWorkFlow);
                    CMCraneWorkFlow cmCraneWorkFlowL = new CMCraneWorkFlow(hatchId, aboveOrBelow, CWPDomain.DL_TYPE_LOAD);
                    cmCraneWorkFlowL.setSingle(smartCraneWorkFlowInfo.getSingle());
                    cmCraneWorkFlowL.setTwin(smartCraneWorkFlowInfo.getTwin());
                    cmCraneWorkFlowL.setTandem(Boolean.FALSE);
                    cmCraneWorkFlowL.setLdStrategy(ldStrategy);
                    workingData.addCMCraneWorkFlow(cmCraneWorkFlowL);
                }
            } catch (Exception e) {
                logger.logError("解析桥作业工艺设置(berthId:" + berthId + ", hatchId:" + hatchId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseParameter(List<SmartCwpParameterInfo> smartCwpParameterInfoList, AllRuntimeData allRuntimeData) {
        //test
        if (ValidatorUtil.isEmpty(smartCwpParameterInfoList)) {
            for (Long berthId : allRuntimeData.getAllBerthId()) {
                SmartCwpParameterInfo smartCwpParameterInfo = new SmartCwpParameterInfo();
                smartCwpParameterInfo.setBerthId(berthId);
                smartCwpParameterInfoList.add(smartCwpParameterInfo);
            }
        }
        logger.logError("输入数据中没有CWP算法配置参数信息", ValidatorUtil.isEmpty(smartCwpParameterInfoList));
        for (SmartCwpParameterInfo smartCwpParameterInfo : smartCwpParameterInfoList) {
            Long berthId = smartCwpParameterInfo.getBerthId();
            try {
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    CwpConfig cwpConfig = new CwpConfig();

                    //特殊因素影响效率的影响因子，1.0
                    Double impactFactor = smartCwpParameterInfo.getImpactFactor();
                    impactFactor = impactFactor != null ? impactFactor : CWPDefaultValue.impactFactor;
                    cwpConfig.setImpactFactor(impactFactor);

                    //重量差，千克kg
                    Integer twinWeightDiff = smartCwpParameterInfo.getTwinWeightDiff();
                    twinWeightDiff = twinWeightDiff != null ? twinWeightDiff : CWPDefaultValue.twinWeightDiff;
                    cwpConfig.setTwinWeightDiff(twinWeightDiff);
                    //桥机安全距离，14米
                    Double craneSafeSpan = smartCwpParameterInfo.getSafeDistance();
                    craneSafeSpan = craneSafeSpan != null ? craneSafeSpan : CWPDefaultValue.craneSafeSpan;
                    cwpConfig.setSafeDistance(craneSafeSpan);
                    ///桥机跨机械起趴大梁移动时间，900s
                    Long crossBarTime = smartCwpParameterInfo.getCrossBarTime();
                    crossBarTime = crossBarTime != null ? crossBarTime : CWPDefaultValue.crossBarTime;
                    cwpConfig.setCrossBarTime(crossBarTime);
                    //桥机移动速度，0.75m/s
                    Double craneSpeed = smartCwpParameterInfo.getCraneMoveSpeed();
                    craneSpeed = craneSpeed != null ? craneSpeed : CWPDefaultValue.craneSpeed;
                    cwpConfig.setCraneMoveSpeed(craneSpeed);

                    //所有桥机平均效率，30关/小时
                    cwpConfig.setOneCntTime((long) (CWPDefaultValue.oneCntWorkTime * impactFactor));
                    //甲板上拆锁时间，甲板五层高及以上集装箱拆锁用时，90s
                    Long unlockTwistTime = smartCwpParameterInfo.getUnlockTwistTime();
                    unlockTwistTime = unlockTwistTime != null ? unlockTwistTime : CWPDefaultValue.unlockTwistTime;
                    cwpConfig.setUnlockTwistTime((long) (unlockTwistTime * impactFactor));
                    //桥机作业单块舱盖板时间，240s
                    Long hatchCoverTime = smartCwpParameterInfo.getHatchCoverTime();
                    hatchCoverTime = hatchCoverTime != null ? hatchCoverTime : CWPDefaultValue.hatchCoverCloseTime;
                    cwpConfig.setHatchCoverTime((long) (hatchCoverTime * impactFactor));
                    //单20尺普通箱作业用时，120s
                    Long single20Time = smartCwpParameterInfo.getSingle20Time();
                    single20Time = single20Time != null ? single20Time : CWPDefaultValue.single20Time;
                    cwpConfig.setSingle20Time((long) (single20Time * impactFactor));
                    //单20尺垫脚箱作业用时，180s
                    Long single20FootPadTime = smartCwpParameterInfo.getSingle20FootPadTime();
                    single20FootPadTime = single20FootPadTime != null ? single20FootPadTime : CWPDefaultValue.single20FootPadTime;
                    cwpConfig.setSingle20FootPadTime((long) (single20FootPadTime * impactFactor));
                    //单20尺全隔槽作业用时，180s
                    Long single20SeparateTime = smartCwpParameterInfo.getSingle20SeparateTime();
                    single20SeparateTime = single20SeparateTime != null ? single20SeparateTime : CWPDefaultValue.single20SeparateTime;
                    cwpConfig.setSingle20SeparateTime((long) (single20SeparateTime * impactFactor));
                    //单40尺普通箱作业用时，120s
                    Long single40Time = smartCwpParameterInfo.getSingle40Time();
                    single40Time = single40Time != null ? single40Time : CWPDefaultValue.single40Time;
                    cwpConfig.setSingle40Time((long) (single40Time * impactFactor));
                    //单45尺普通箱作业用时，120s
                    Long single45Time = smartCwpParameterInfo.getSingle45Time();
                    single45Time = single45Time != null ? single45Time : CWPDefaultValue.single45Time;
                    cwpConfig.setSingle45Time((long) (single45Time * impactFactor));
                    //双20尺普通箱作业用时，150s
                    Long double20Time = smartCwpParameterInfo.getDouble20Time();
                    double20Time = double20Time != null ? double20Time : CWPDefaultValue.double20Time;
                    cwpConfig.setDouble20Time((long) (double20Time * impactFactor));
                    //双吊具40尺作业用时，140s
                    Long double40Time = smartCwpParameterInfo.getDouble40Time();
                    double40Time = double40Time != null ? double40Time : CWPDefaultValue.double40Time;
                    cwpConfig.setDouble40Time((long) (double40Time * impactFactor));
                    //双吊具45尺作业用时，140s
                    Long double45Time = smartCwpParameterInfo.getDouble45Time();
                    double45Time = double45Time != null ? double45Time : CWPDefaultValue.double45Time;
                    cwpConfig.setDouble45Time((long) (double45Time * impactFactor));
                    //超限箱、分体大件作业用时，360s
                    Long specialCntTime = smartCwpParameterInfo.getSpecialCntTime();
                    specialCntTime = specialCntTime != null ? specialCntTime : CWPDefaultValue.specialCntTime;
                    cwpConfig.setSpecialCntTime((long) (specialCntTime * impactFactor));
                    //直装直提危险品作业用时，360s
                    Long dangerCntTime = smartCwpParameterInfo.getDangerCntTime();
                    dangerCntTime = dangerCntTime != null ? dangerCntTime : CWPDefaultValue.dangerCntTime;
                    cwpConfig.setDangerCntTime((long) (dangerCntTime * impactFactor));
                    //桥机换倍船扫时间，300s
                    Long hatchScanTime = smartCwpParameterInfo.getHatchScanTime();
                    hatchScanTime = hatchScanTime != null ? hatchScanTime : CWPDefaultValue.hatchScanTime;
                    cwpConfig.setHatchScanTime((long) (hatchScanTime * impactFactor));

                    //是否过驾驶台起大梁
                    Boolean crossBridge = smartCwpParameterInfo.getSetupBridge();
                    crossBridge = crossBridge != null ? crossBridge : CWPDefaultValue.crossBridge;
                    cwpConfig.setSetupBridge(crossBridge);
                    //是否过烟囱起大梁
                    Boolean crossChimney = smartCwpParameterInfo.getSetupChimney();
                    crossChimney = crossChimney != null ? crossChimney : CWPDefaultValue.crossChimney;
                    cwpConfig.setSetupChimney(crossChimney);
                    //装卸策略，即边装边卸：BLD、一般装卸：LD
                    String ldStrategy = smartCwpParameterInfo.getLdStrategy();
                    ldStrategy = StringUtil.isNotBlank(ldStrategy) ? ldStrategy.equals("LD") ? CWPDomain.LD_STRATEGY_LD : CWPDomain.LD_STRATEGY_BLD : CWPDomain.LD_STRATEGY_BLD;
                    cwpConfig.setLdStrategy(ldStrategy);
                    //建议开路数
                    Integer craneAdviceNumber = smartCwpParameterInfo.getCraneAdviceNumber();
                    cwpConfig.setCraneAdviceNumber(craneAdviceNumber);

                    //船舶开路装卸平衡考虑参数。首次开路全装、首次开路全卸、首次开路装卸错开:"L"、"D"、"LD"
                    cwpConfig.setLoadPrior(smartCwpParameterInfo.getLoadPrior());
                    //均衡每部桥吊的作业量，整船桥吊同时完工，Y/N
                    String craneSameWorkTime = smartCwpParameterInfo.getCraneSameWorkTime();
                    craneSameWorkTime = StringUtil.isNotBlank(craneSameWorkTime) ? CWPDomain.YES.equals(craneSameWorkTime) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.craneSameWorkTime;
                    cwpConfig.setCraneSameWorkTime(craneSameWorkTime);
                    //避免开工作业甲板装船箱，避让时间为开工后一小时，Y/N
                    String deckWorkLater = smartCwpParameterInfo.getDeckWorkLater();
                    deckWorkLater = StringUtil.isNotBlank(deckWorkLater) ? CWPDomain.YES.equals(deckWorkLater) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.deckWorkLater;
                    cwpConfig.setDeckWorkLater(deckWorkLater);
                    //重点路单桥吊持续作业，其余箱量由左右桥吊分配， Y/N
                    String mainRoadOneCrane = smartCwpParameterInfo.getMainRoadOneCrane();
                    mainRoadOneCrane = StringUtil.isNotBlank(mainRoadOneCrane) ? CWPDomain.YES.equals(mainRoadOneCrane) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.mainRoadOneCrane;
                    cwpConfig.setMainRoadOneCrane(mainRoadOneCrane);
                    //分割舱优先作业设定，Y/N
                    String dividedHatchFirst = smartCwpParameterInfo.getDividedHatchFirst();
                    dividedHatchFirst = StringUtil.isNotBlank(dividedHatchFirst) ? CWPDomain.YES.equals(dividedHatchFirst) ? CWPDomain.YES : CWPDomain.NO : CWPDefaultValue.dividedHatchFirst;
                    cwpConfig.setDividedHatchFirst(dividedHatchFirst);

                    workingData.setCwpConfig(cwpConfig);
                }
            } catch (Exception e) {
                logger.logError("解析算法配置参数(berthId:" + berthId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseVesselContainer(List<SmartVesselContainerInfo> smartVesselContainerInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("进出口船图箱信息", ValidatorUtil.isEmpty(smartVesselContainerInfoList));
        WorkingData workingData;
        for (SmartVesselContainerInfo smartVesselContainerInfo : smartVesselContainerInfoList) {
            Long berthId = smartVesselContainerInfo.getBerthId();
            String vLocation = smartVesselContainerInfo.getvLocation();
            Long vpcCntId = smartVesselContainerInfo.getVpcCntrId();
            String size = smartVesselContainerInfo.getcSzCsizecd();
            String type = smartVesselContainerInfo.getcTypeCd();
            String dlType = smartVesselContainerInfo.getLduldfg();
            String throughFlag = smartVesselContainerInfo.getThroughFlag();
            Long hatchId = smartVesselContainerInfo.getHatchId();
            String workFlow = smartVesselContainerInfo.getWorkflow();
            Long moveOrder = smartVesselContainerInfo.getCwpwkMoveNum();
            Long cntWorkTime = smartVesselContainerInfo.getContainerWorkInterval(); //单位秒
            Double weight = smartVesselContainerInfo.getWeight();
            String dgCd = smartVesselContainerInfo.getDtpDnggcd(); //危险品：
            String isHeight = smartVesselContainerInfo.getIsHeight(); //是否是高箱：Y/N
            String cntHeight = smartVesselContainerInfo.getCntHeightDesc(); //箱子具体高度
            String rfFlag = smartVesselContainerInfo.getRfcfg(); //冷藏标记：Y/N
            String overrunCd = smartVesselContainerInfo.getOvlmtcd(); //超限箱标记：Y/N
            throughFlag = "N".equals(throughFlag) ? CWPDomain.THROUGH_NO : CWPDomain.THROUGH_YES;
            dgCd = StringUtil.isNotBlank(dgCd) ? !"N".equals(dgCd) ? CWPDomain.YES : CWPDomain.NO : CWPDomain.NO;
            overrunCd = StringUtil.isNotBlank(overrunCd) ? !"N".equals(overrunCd) ? CWPDomain.YES : CWPDomain.NO : CWPDomain.NO;
            try {
                workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    VMContainer vmContainer = new VMContainer(vLocation, dlType);
                    if (CWPDomain.THROUGH_NO.equals(throughFlag)) { //非过境箱
                        vmContainer.setThroughFlag(throughFlag);
                        vmContainer.setVpcCntId(vpcCntId);
                        vmContainer.setBerthId(berthId);
                        vmContainer.setHatchId(hatchId);
                        vmContainer.setCntHeight(cntHeight);
                        vmContainer.setCntWorkTime(cntWorkTime);
                        vmContainer.setWeightKg(weight);
                        vmContainer.setSize(size);
                        vmContainer.setType(type);
                        vmContainer.setDgCd(dgCd);
                        vmContainer.setIsHeight(isHeight);
                        vmContainer.setRfFlag(rfFlag);
                        vmContainer.setOverrunCd(overrunCd);
                        workingData.putVMContainer(new VMPosition(vLocation), vmContainer);
                    }
                }
            } catch (Exception e) {
                logger.logError("解析进出口船图箱(berthId:" + berthId + ", vLocation:" + vLocation + ", dlType:" + dlType + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseCranePool(List<SmartVesselCranePoolInfo> smartVesselCranePoolInfoList, List<SmartCranePoolInfo> smartCranePoolInfoList, List<SmartCraneFirstWorkInfo> smartCraneFirstWorkInfoList, AllRuntimeData allRuntimeData) {
        logger.logError("船舶桥机池信息", ValidatorUtil.isEmpty(smartVesselCranePoolInfoList));
        for (SmartVesselCranePoolInfo smartVesselCranePoolInfo : smartVesselCranePoolInfoList) {
            Long berthId = smartVesselCranePoolInfo.getBerthId();
            Long poolId = smartVesselCranePoolInfo.getPoolId();
            try {
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {
                    for (SmartCranePoolInfo smartCranePoolInfo : smartCranePoolInfoList) {
                        if (poolId.equals(smartCranePoolInfo.getPoolId())) {
                            String craneNo = smartCranePoolInfo.getCraneNo();
                            logger.logError("桥机池中桥机-桥机号为null", ValidatorUtil.isNull(craneNo));
                            CMCranePool cmCranePool = new CMCranePool(poolId, craneNo);
                            cmCranePool = (CMCranePool) BeanCopyUtil.copyBean(smartCranePoolInfo, cmCranePool);
                            if (StringUtil.isNotBlank(smartVesselCranePoolInfo.getFirstCraneNos())) {
                                cmCranePool.setFirstCraneFlag(smartVesselCranePoolInfo.getFirstCraneNos().contains(craneNo));
                            } else {
                                cmCranePool.setFirstCraneFlag(Boolean.TRUE);
                            }
                            //桥机第一次选择的倍位、关号
                            for (SmartCraneFirstWorkInfo smartCraneFirstWorkInfo : smartCraneFirstWorkInfoList) {
                                if (berthId.equals(smartCraneFirstWorkInfo.getBerthId()) && craneNo.equals(smartCraneFirstWorkInfo.getCraneNo())) {
                                    if (StringUtil.isNotBlank(smartCraneFirstWorkInfo.getFirstWorkBayNo()) && smartCraneFirstWorkInfo.getFirstWorkAmount() != null) {
                                        cmCranePool.setFirstWorkBayNo(Integer.valueOf(smartCraneFirstWorkInfo.getFirstWorkBayNo()));
                                        cmCranePool.setFirstWorkAmount(smartCraneFirstWorkInfo.getFirstWorkAmount());
                                        break;
                                    }
                                }
                            }
                            workingData.addCMCranePool(cmCranePool);
                        }
                    }
                }
            } catch (Exception e) {
                logger.logError("解析船舶桥机池(berthId:" + berthId + ", poolId:" + poolId + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseCraneMaintainPlan(List<SmartCraneMaintainPlanInfo> smartCraneMaintainPlanInfoList, AllRuntimeData allRuntimeData) {
        for (SmartCraneMaintainPlanInfo smartCraneMaintainPlanInfo : smartCraneMaintainPlanInfoList) {
            String craneNo = smartCraneMaintainPlanInfo.getCraneNo();
            try {
                CMCraneMaintainPlan cmCraneMaintainPlan = new CMCraneMaintainPlan(craneNo);
                cmCraneMaintainPlan.setMaintainStartTime(smartCraneMaintainPlanInfo.getMaintainStartTime());
                cmCraneMaintainPlan.setMaintainEndTime(smartCraneMaintainPlanInfo.getMaintainEndTime());
                cmCraneMaintainPlan.setCraneStatus(smartCraneMaintainPlanInfo.getCraneStatus());
                cmCraneMaintainPlan.setCraneMoveStatus(smartCraneMaintainPlanInfo.getCraneMoveStatus());
//                allRuntimeData.addCMCraneMaintainPlan(cmCraneMaintainPlan);
            } catch (Exception e) {
                logger.logError("解析桥机维修计划(craneNo:" + craneNo + ")信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

    private void parseAreaCnt(List<SmartAreaContainerInfo> smartAreaContainerInfoList, AllRuntimeData allRuntimeData) {
        for (SmartAreaContainerInfo smartAreaContainerInfo : smartAreaContainerInfoList) {
            Long berthId = smartAreaContainerInfo.getBerthId();
            String areaNo = smartAreaContainerInfo.getAreaNo();
            try {
                WorkingData workingData = allRuntimeData.getWorkingDataByBerthId(berthId);
                if (workingData != null) {

                }
                AreaContainer areaContainer = new AreaContainer();

            } catch (Exception e) {
                logger.logError("解析堆场箱区(areaNo:" + areaNo + ")统计信息过程中发生数据异常！");
                e.printStackTrace();
            }
        }
    }

}
