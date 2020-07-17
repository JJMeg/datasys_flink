package com.jjmeg.feeder.datasys.flink.common.output;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashMap;
import java.util.List;

/**
 * @author hexiaoying10
 * @create 2020/07/16 11:29
 */

public class Kafka08Output extends OutputBase {

    public boolean init(StreamExecutionEnvironment env, HashMap<String, String> fixedArgs, HashMap<String, String> args) {
        System.out.println("init output....");
        return true;
    }

    public void process(List<Object> inputList) {

        System.out.println("onput process.....");
        for (Object input : inputList) {

        }
    }
}

