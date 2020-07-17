package com.jjmeg.feeder.datasys.flink.common.input;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;


/**
 * @author hexiaoying10
 * @create 2020/07/09 10:33
 */
public abstract class InputBase implements Serializable {
    public InputBase() {
    }

    public abstract boolean init(StreamExecutionEnvironment env, HashMap<String, String> fixArgs, HashMap<String, String> args);

    public abstract Object process(List<Object> inputs) throws Exception;

}
