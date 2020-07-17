import com.jjmeg.feeder.datasys.flink.common.util.ConfigurationException;
import com.jjmeg.feeder.datasys.flink.common.util.Node;
import com.jjmeg.feeder.datasys.flink.common.util.Parser;
import org.junit.Test;

import java.util.Map;

/**
 * @author hexiaoying10
 * @create 2020/07/08 15:29
 */
public class ElementTest {
    @Test
    public void test() throws ConfigurationException {
        String xmlPath = "E:\\Feed\\datasys_flink\\config\\backend.xml";

        String nodeName = "weiflink";
        String paramsStr = "input=dt=2018010110^output=dt=2018010110,2018010111";

        Map<String, String> outerParams = null;

        Node node = Parser.parse(xmlPath, nodeName, outerParams);

        System.out.println("Node：" + node);
    }
}
