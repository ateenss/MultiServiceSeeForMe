package com.cwp3.single.service.impl;

import com.cwp3.data.AllRuntimeData;
import com.cwp3.single.data.MoveResults;

/*
用于多线程计算
计算单个舱的总量与可作业量
考虑到线程安全，写入对象必须是线程安全的
 */
public class MoveServiceRunner implements Runnable {


    AllRuntimeData allRuntimeData;
    MoveResults moveResults;

    @Override
    public void run() {


    }
}
