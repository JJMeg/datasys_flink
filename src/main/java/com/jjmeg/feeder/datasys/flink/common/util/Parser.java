package com.jjmeg.feeder.datasys.flink.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.*;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:07
 */
public class Parser {

    static final ClassLoader loader = Parser.class.getClassLoader();
    static final Logger logger = LoggerFactory.getLogger(Parser.class);
    public static Map<String, String> outerParams;

    public static String getOuterParam(String intput) throws ConfigurationException {


        if (!intput.startsWith("$") || outerParams == null || outerParams.size() == 0) {
            return intput;
        } else {
            if (intput.length() > 1) {
                String key = intput.substring(1);
                if (outerParams.containsKey(key)) {
                    return outerParams.get(key);
                } else {
                    throw new ConfigurationException();
                }
            } else {
                return intput;
            }
        }
    }

    public static Node parse(String filePath, String nodeName, Map<String, String> outerParams) throws ConfigurationException {
        Parser.outerParams = outerParams;
        SAXReader saxReader = new SAXReader();

        Document doc;
        try {
            doc = saxReader.read(new File(filePath));
        } catch (DocumentException e) {
            System.out.println(e);
            logger.error("config format error!", e);
            throw new ConfigurationException();
        }

        Element root = doc.getRootElement();

        Element nodesElm = root.element("nodes");


        if (nodesElm == null) {
            logger.error("the configuration must contains tag:configuration/dag and tag:configuration/nodes");
            throw new ConfigurationException();
        }

        List<Element> nodesElement = nodesElm.elements("node");

        for (Element n : nodesElement) {
            try {

                String name = n.attributeValue("name");
                if (name.equals(nodeName)) {
                    Node ret = buildNode(n);
                    return ret;
                }
            } catch (Exception e) {
                logger.error("the tag:configuration/nodes/node must contains attribute:name and attribute:type.");
                throw new ConfigurationException();
            }
        }

        return null;
    }


    public static Node buildNode(Element nodeElement) throws ConfigurationException {

        String name, type;
        try {
            type = nodeElement.attributeValue("type");
            name = nodeElement.attributeValue("name");
        } catch (Exception e) {
            logger.error("the tag:configuration/nodes/node must contains attribute:name and attribute:type.");
            throw new ConfigurationException();
        }

        Node node = new Node(name);


        Element dag = nodeElement.element("dag");
        Element runtime = nodeElement.element("runtime");

        if (runtime == null) {
            logger.error("the configuration must contains tag:configuration/nodes/node/dag and tag:configuration/nodes/node/runtime");
            throw new ConfigurationException();
        }

        List<Element> tasks = nodeElement.elements("task");

        try {

            DAG<Task> taskDAG = buildTaskDAG(dag, tasks);

            System.out.println("taskDAG: " + taskDAG);
            node.dag = taskDAG;
            node.type = Node.TYPE.valueOf(type.toUpperCase());


            Element properties = nodeElement.element("properties");
            Map<String, String> argsMap = new HashMap<>();
            for (Element e : (List<Element>) properties.elements()) {
                argsMap.put(e.getName(), e.getText());
            }

            node.properties = argsMap;


        } catch (Exception e) {
            logger.warn("the tag:configuration/nodes/node/properties dose not exist.");

        }

        try {
            RunTime rt = new RunTime();

            rt.binPath = runtime.element("binPath").getText();
            rt.env = runtime.element("env").getText();

            Element properties = runtime.element("args");
            Map<String, String> argsMap = new HashMap<>();
            for (Element e : (List<Element>) properties.elements()) {
                argsMap.put(e.getName(), getOuterParam(e.getText()));
            }
            rt.args = argsMap;
            node.runTime = rt;

        } catch (Exception e) {
            logger.error("the tag:configuration/nodes/node/runtime must contains tag:configuration/nodes/node/runtime/{binPath, env, args}.");
            throw new ConfigurationException();
        }


        return node;
    }

    public static Task buildTask(Element taskElement) throws ConfigurationException {

        String name, type;
        try {
            type = taskElement.attributeValue("type");
            name = taskElement.attributeValue("name");
        } catch (Exception e) {
            logger.error("the tag:configuration/nodes/node/task must contains attribute:type and attribute:name.");
            throw new ConfigurationException();
        }


        String className;

        Map<String, String> argsMap = new HashMap<>();
        Map<String, String> fixedArgsMap = new HashMap<>();

        try {
            className = taskElement.element("className").getText();

            Element fixedArgs = taskElement.element("fixedArgs");
            for (Element e : (List<Element>) fixedArgs.elements()) {
                fixedArgsMap.put(e.getName(), getOuterParam(e.getText()));
            }


            Element args = taskElement.element("args");
            for (Element e : (List<Element>) args.elements()) {
                argsMap.put(e.getName(), getOuterParam(e.getText()));
            }

        } catch (Exception e) {
            logger.error("the tag:configuration/nodes/node/task must contains tag:className and args:name.");
            throw new ConfigurationException();
        }


        Task task = new Task(name);
        task.className = className;
        task.args = argsMap;
        task.fixedArgs = fixedArgsMap;
        task.type = Task.TYPE.valueOf(type.toUpperCase());


        return task;
    }

    public static DAG<Task> buildTaskDAG(Element dagElement, List<Element> tasksElement) throws ConfigurationException {
        DAG<Task> dag = new DAG<>();
        Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();
        try {


            List<Element> nodes = dagElement.elements("task");
            for (Element e : nodes) {
                Set<String> dependenciesAttribute = new HashSet<String>(
                        Arrays.asList(
                                e.attributeValue("dependencies").trim().split(",")
                        )
                );

                dependencies.put(e.getText(), dependenciesAttribute);
            }
        } catch (Exception e) {
            logger.error("the tag:configuration/nodes/node/dag must contains at least one tag:task and attribute:dependencies.");
            throw new ConfigurationException();
        }
        //List<Element> nodesEle = dagElement.elements("node");


        Map<String, Task> nodeMap = new HashMap<String, Task>();
        for (Element n : tasksElement) {

            Task node = buildTask(n);
            try {
                nodeMap.put(n.attributeValue("name"), node);
            } catch (Exception e) {
                logger.error("the tag:configuration/nodes/node/task must contains attribute:name.");
                throw new ConfigurationException();
            }

        }


        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            if (!nodeMap.containsKey(entry.getKey())) {
                continue;
            }
            Task key = nodeMap.get(entry.getKey());
            Set<Task> nodeSet = new HashSet<>();

            for (String nodeName : entry.getValue()) {
                if (nodeMap.containsKey(nodeName)) {
                    nodeSet.add(nodeMap.get(nodeName));
                } else {

                }
            }


            //System.out.println(nodeSet);
            if (nodeSet.isEmpty()) {
                dag.insert(key);
            } else {
                dag.insert(key, nodeSet);
            }


        }
        return dag;
    }

}
