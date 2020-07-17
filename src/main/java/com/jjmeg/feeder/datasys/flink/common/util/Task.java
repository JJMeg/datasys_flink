package com.jjmeg.feeder.datasys.flink.common.util;

import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:09
 */
public class Task extends Vertex {
    public String className;

    public Map<String, String> args;
    public Map<String, String> fixedArgs;
    public TYPE type;

    public Task(String name) {
        super(name);
    }


    public String toString() {
        return "Task{" +
                "className='" + className + '\'' +
                ", args=" + args +
                ", fixedArgs=" + fixedArgs +
                ", name='" + name + '\'' +
                '}';
    }

    public enum TYPE {

        FLINKINPUT,
        FLINKPROCESS,
        FLINKOUTPUT
    }

}
