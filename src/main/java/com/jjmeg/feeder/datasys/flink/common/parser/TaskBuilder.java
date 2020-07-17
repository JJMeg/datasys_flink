package com.jjmeg.feeder.datasys.flink.common.parser;

import com.jjmeg.feeder.datasys.flink.common.util.*;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:05
 */
public class TaskBuilder {
    static final ClassLoader loader = Parser.class.getClassLoader();
    static final Logger logger = Logger.getLogger(TaskBuilder.class);

    public Object invokeInit(Object obj, String methodName, String jobType, Object... args) throws Exception {
        Class[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                parameterTypes[i] = (jobType != null && jobType.equals("DataSet")) ? ExecutionEnvironment.class : StreamExecutionEnvironment.class;
            } else {
                parameterTypes[i] = args[i].getClass();
            }
            //System.out.println(parameterTypes[i]);
        }

        Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
        return method.invoke(obj, args);
    }

    public Object invokeInputInit(Object obj, String methodName, Object... args) throws Exception {
        Class[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (i == 0) {
                parameterTypes[i] = StreamExecutionEnvironment.class;
            } else {
                parameterTypes[i] = args[i].getClass();
            }
            //System.out.println(parameterTypes[i]);
        }

        Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
        return method.invoke(obj, args);
    }

    public Object invokeProcess(Object obj, String methodName, Object... args) throws Exception {
        Class[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = List.class;
            //System.out.println(parameterTypes[i]);
        }

        Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
        return method.invoke(obj, args);
    }

    public Object invoke(String className) {
        Object obj = null;
        try {
            obj = Class.forName(className).newInstance();
            //调用上一个方法
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean build(String xmlFilePath, String nodeName, Map<String, String> outerParams) throws Exception {
        try {
            Node node = Parser.parse(xmlFilePath, nodeName, outerParams);

            System.out.println("Node：" + node);
            if (node == null) {
                logger.error("Node " + nodeName + "is not exist.");
                return false;
            }

            String jobName = node.properties.get("jobName");
            System.out.println("============================= Flink Job Name is :" + jobName);
            if ("".equals(jobName)) {
                System.out.println("============================= Flink Job Name is : null");
                return false;
            }
            System.out.println();

            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


            System.out.println("============================= task info =============================");
            Map<String, Object> result = new HashMap<String, Object>();
            while (node.dag.hasNextRunnableTask()) {
                Task task = node.dag.nextRunnableTask();
                System.out.println("== task name: " + task.name);
                Object obj = invoke(task.className);

                if (task.type.equals(Task.TYPE.FLINKINPUT)) {
                    invokeInputInit(obj, "init", env, task.fixedArgs, task.args);
                } else {
                    invokeInit(obj, "init", "", (StreamExecutionEnvironment)env, task.fixedArgs, task.args);
                }

                Object input = new Object();
                List inputs = new ArrayList<Object>();
                //System.out.println(node.dag.fixedDependencies.get(task));

                for (Vertex d : node.dag.fixedDependencies.get(task)) {
                    input = result.get(d.name);
                    inputs.add(input);
                }

                Object re = invokeProcess(obj, "process", inputs);
                result.put(task.name, re);
                node.dag.notifyDone(task);
            }
            System.out.println("============================= task info =============================");
            System.out.println();


        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        String xmlPath = "E:\\Feed\\datasys_flink\\config\\test.xml";

        String nodeName = "weiflink";
        String paramsStr = "input=dt=2018010110^output=dt=2018010110,2018010111";

        Map<String, String> outerParams = null;

        // 本地测试时指定xmlpath为本地目录
        if (args.length > 1) {
            xmlPath = args[0];
            nodeName = args[1];

            if (args.length > 2) {
                outerParams = parseOuterParams(args[2]);
            }
            new TaskBuilder().build(xmlPath, nodeName, outerParams);
        } else if (!xmlPath.isEmpty()) {
            new TaskBuilder().build(xmlPath, nodeName, outerParams);
        } else {
            System.out.println("parameter error, xml and nodeName needed!");
            System.exit(-1);
        }


    }

    static Map<String, String> parseOuterParams(String params) {
        Map<String, String> outerParams = new HashMap<>();
        if (params != null && params.length() > 0) {
            String[] arr = params.trim().split("\\^");
            for (String kv : arr) {

                int index = kv.indexOf('=');
                System.out.println(kv + "-" + index);
                if (index > 0 && index < kv.length()) {
                    System.out.println(kv + "-" + index + "-" + kv.substring(index + 1));
                    outerParams.put(kv.substring(0, index), kv.substring(index + 1));
                }

            }
        }
        return outerParams;
    }
}
