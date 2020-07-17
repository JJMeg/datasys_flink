package com.jjmeg.feeder.datasys.flink.common.output;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/16 11:28
 */
public abstract class OutputBase implements Serializable {
    protected String _emitType = null;
    protected String _emitSchema = null;
    protected String _dsName = null;
    protected Map<String, String> _args = null;

    public OutputBase() {
    }

    public abstract boolean init(StreamExecutionEnvironment env, HashMap<String, String> fixArgs, HashMap<String, String> args);

    public abstract void process(List<Object> inputs);
}