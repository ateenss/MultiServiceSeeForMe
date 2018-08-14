package com.cwp3.utils;

/**
 * Created by csw on 2017/9/20.
 * Description:
 */
public class StringUtil {

    public static String getKey(Long id, String str) {
        return id.toString() + "@" + str;
    }

    public static String getKey(String id, String str) {
        return id + "@" + str;
    }

    public static String getKey(String str, Integer id) {
        return str + "@" + id.toString();
    }

    public static String getKey(Integer id, String str) {
        return id.toString() + "@" + str;
    }

    public static String getKey(Integer id1, Integer id2) {
        return id1.toString() + "@" + id2.toString();
    }

    public static String getSecondKey(String key) {
        return key.split("@")[1];
    }

    public static String getFirstKey(String key) {
        return key.split("@")[0];
    }

    public static boolean isNotBlank(String str) {
        return str != null && !"".equals(str.trim());
    }
}
