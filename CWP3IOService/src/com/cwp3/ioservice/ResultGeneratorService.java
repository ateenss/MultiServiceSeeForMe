package com.cwp3.ioservice;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.single.data.MoveData;

/**
 * Created by csw on 2018/6/7.
 * Description:
 */
public interface ResultGeneratorService {

    void generateCwpResult(MoveData moveData, WorkingData workingData);
}
