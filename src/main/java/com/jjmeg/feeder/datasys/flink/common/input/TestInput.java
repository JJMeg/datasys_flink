package com.jjmeg.feeder.datasys.flink.common.input;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashMap;
import java.util.List;

/**
 * @author hexiaoying10
 * @create 2020/07/16 10:59
 */
public class TestInput extends InputBase {
    @Override
    public boolean init(StreamExecutionEnvironment env, HashMap<String, String> fixArgs, HashMap<String, String> args) {
        System.out.println("init input.....");
        return true;
    }

    @Override
    public Object process(List<Object> inputs) throws Exception {

        System.out.println("process.....");
        return null;
    }
}
