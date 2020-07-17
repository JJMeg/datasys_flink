package com.jjmeg.feeder.datasys.flink.common.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/16 11:23
 */

public abstract class udfBase implements Serializable {
    protected String _emitType = null;
    protected String _emitSchema = null;
    protected String _dsName = null;
    protected Map<String, String> _args = null;

    public udfBase() {
    }

    public abstract boolean init(HashMap<String, String> fixArgs, HashMap<String, String> args);

    public abstract Object process(List<Object> inputs);

}
