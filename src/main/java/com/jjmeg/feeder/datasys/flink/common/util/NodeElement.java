package com.jjmeg.feeder.datasys.flink.common.util;

import java.util.Set;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:10
 */
public class NodeElement extends Vertex {
    private String className;
    private Set<NodeElement> dependencies;
    private String args;

    public NodeElement(String name) {
        super(name);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setDependencies(Set<NodeElement> dependencies) {
        this.dependencies = dependencies;
    }

    public void setArgs(String args) {
        this.args = args;
    }
}
