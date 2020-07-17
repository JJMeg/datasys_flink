package com.jjmeg.feeder.datasys.flink.common.util;

import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:08
 */
public class RunTime {
    public String env;
    public String binPath;
    public Map<String, String> args;

    @Override
    public String toString() {
        return "RunTime{" +
                "env='" + env + '\'' +
                ", binPath='" + binPath + '\'' +
                ", args=" + args +
                '}';
    }
}
