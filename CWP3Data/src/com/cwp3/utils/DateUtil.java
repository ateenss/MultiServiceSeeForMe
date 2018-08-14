package com.cwp3.utils;

import java.util.Date;

/**
 * Created by csw on 2017/12/13.
 * Description:
 */
public class DateUtil {

    public static long getSecondTime(Date date) {
        return date.getTime() / 1000;
    }
}
