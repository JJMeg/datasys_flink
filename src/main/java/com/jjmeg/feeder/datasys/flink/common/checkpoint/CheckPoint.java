package com.jjmeg.feeder.datasys.flink.common.checkpoint;

import com.jjmeg.feeder.datasys.flink.common.util.Constant;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/08 14:24
 */
public class CheckPoint {
    public static StreamExecutionEnvironment setCheckPoint(StreamExecutionEnvironment env, Map<String, String> properties, String jobName) {
        // 为了能够使用支持容错的kafka Consumer，需要开启checkpoint
        // checkpoint配置
        // 每10分钟 checkpoint一次-600000
        // StreamExecutionEnvironment.enableCheckpointing(internal，mode)启用checkpoint。 internal默认是-1，表示checkpoint不开启
        System.out.println("============================= CheckPoint info =======================");
        if (!"".equals(properties.get(Constant.CHECK_POINT_TIME))) {
            System.out.println("== enableCheckpointing:" + properties.get(Constant.CHECK_POINT_TIME));
            env.enableCheckpointing(Long.valueOf(properties.get(Constant.CHECK_POINT_TIME)));
        } else {
            // 默认为10分钟
            System.out.println("== enableCheckpointing:" + 600000);
            env.enableCheckpointing(600000);
        }

        // 设置模式为exactly-once （这是默认值）
        if ("EXACTLY_ONCE".equals(properties.get(Constant.CHECK_POINTING_MODE))) {
            // 设置模式为exactly-once （这是默认值）
            System.out.println("== setCheckpointingMode:" + "EXACTLY_ONCE");
            env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        } else {
            System.out.println("== setCheckpointingMode:" + "AT_LEAST_ONCE");
            env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
        }

        // 检查点必须在3分钟内完成，或者被丢弃--一般要配成checkpoint间隔的30%以下--200000
        if (!"".equals(properties.get(Constant.CHECK_POINT_TIMEOUT))) {
            System.out.println("== setCheckpointTimeout:" + properties.get(Constant.CHECK_POINT_TIMEOUT));
            env.getCheckpointConfig().setCheckpointTimeout(Long.valueOf(properties.get(Constant.CHECK_POINT_TIMEOUT)));
        } else {
            // 默认为3分钟
            System.out.println("== setCheckpointTimeout:" + 180000);
            env.getCheckpointConfig().setCheckpointTimeout(180000);
        }

        // 确保检查点之间有进行500 ms的进度，如果设置的enableCheckpointing是分钟级别的话，这个参数可以不设置。
        // env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000);
        // 同一时间只允许进行一个检查点
        // env.getCheckpointConfig().setMaxConcurrentCheckpoints(0);

        // 用于开启checkpoints的外部持久化，但是在job失败的时候不会自动清理，需要自己手工清理state
        // ExternalizedCheckpointCleanup用于指定当job canceled的时候externalized checkpoint该如何清理，
        // DELETE_ON_CANCELLATION的话，在job canceled的时候会自动删除externalized state，但是如果是FAILED的状态则会保留；
        // RETAIN_ON_CANCELLATION则在job canceled的时候会保留externalized checkpoint state
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        System.out.println("== enableExternalizedCheckpoints:" + "ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION");

        // ProcessingTime,IngestionTime,EventTime
        String StreamTimeCharacteristic = properties.get(Constant.STREAM_TIME_CHARACTERISTIC);
        if (Constant.PROCESSING_TIME.equals(StreamTimeCharacteristic)) {
            env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        } else if (Constant.EVENT_TIME.equals(StreamTimeCharacteristic)) {
            env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        } else if (Constant.INGESTION_TIME.equals(StreamTimeCharacteristic)) {
            env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
        }
        System.out.println("== setStreamTimeCharacteristic:" + env.getStreamTimeCharacteristic());


        System.out.println("============================= CheckPoint info =======================");
        System.out.println();
        return env;
    }

}
