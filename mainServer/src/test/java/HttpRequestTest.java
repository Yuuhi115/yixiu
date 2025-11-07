import gdufs.yixiu.util.HttpUtils;
import gdufs.yixiu.util.HttpsUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class HttpRequestTest {
    @Test
    public void doGetTest() throws Exception {
        String email = "1521427714@qq.com";
        Map<String, String> query = new HashMap<String, String>();
        query.put("email", email);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        HttpResponse response = HttpUtils.doGet("https://gdufsyixiu.cn", "/send/emailVerification", "GET", headers, query);
        System.out.println(response.toString());
    }
    @Test
    public void doGetTest2() throws Exception {
        String url = "https://gdufsyixiu.cn/"; // 可换成具体 API 路径
        String result = HttpsUtils.doGet(url);
        System.out.println(result);
    }

}
