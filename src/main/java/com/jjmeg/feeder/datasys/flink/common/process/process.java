package com.jjmeg.feeder.datasys.flink.common.process;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashMap;
import java.util.List;

/**
 * @author hexiaoying10
 * @create 2020/07/16 11:23
 */
public class process extends udfBase {

    public boolean init(StreamExecutionEnvironment env, HashMap<String, String> fixArgs, HashMap<String,String> args) {
        System.out.println("init process.....");
        return false;
    }

    @Override
    public boolean init(HashMap<String, String> fixArgs, HashMap<String, String> args) {
        return false;
    }

    @Override
    public Object process(List<Object> inputs) {

        System.out.println("process process.....");
        return null;
    }
}
