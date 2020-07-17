package com.jjmeg.feeder.datasys.flink.common.util;

import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:08
 */
public class Node extends Vertex {
    public RunTime runTime;
    public TYPE type;

    public DAG<Task> dag;

    @Override
    public String toString() {
        return "Node{" +
                "runTime=" + runTime +
                ", type=" + type +
                ", properties=" + properties +
                '}';
    }

    public Map<String, String> properties;

    public Node(String name) {
        super(name);
    }

    public enum TYPE {
        WEISAPRK,
        WEILEARN,
        WEIPIG,
        WEIFLINK
    }

    public static Node get(String name, DAG<Node> dag) {
        while (dag.hasNextRunnableTask()) {
            Node node = dag.nextRunnableTask();

            if (node.name.equals(name)) {
                return node;
            }

            dag.notifyDone(node);
        }
        return null;
    }
}
