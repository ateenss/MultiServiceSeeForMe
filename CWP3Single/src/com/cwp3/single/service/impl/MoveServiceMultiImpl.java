package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MoveServiceMultiImpl {

    ExecutorService executorService;


    public MoveServiceMultiImpl() {
        int numOfThreads = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(numOfThreads);
    }

    /**
     * 计算单船的总量和可作业量
     * 采用并行计算
     * @param allRuntimeData
     * @param berthId
     */
    public void calculateMovesByVessel(AllRuntimeData allRuntimeData,Long berthId){
        //获取船的bayNo
        String vesselCode = allRuntimeData.getWorkingDataByBerthId(berthId).getVmSchedule().getVesselCode();



        //生成任务
        MoveServiceRunner moveServiceRunner = new MoveServiceRunner();



        executorService.submit(moveServiceRunner);


        executorService.shutdown();
        try{
            if(!executorService.awaitTermination(20, TimeUnit.SECONDS)){
                executorService.shutdownNow();
            }
            Thread.sleep(1000);

        }catch (Exception e){
            //time out
            e.printStackTrace();
            executorService.shutdownNow();
        }

        //calculate down,do something





    }
}
