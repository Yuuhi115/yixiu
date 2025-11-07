package gdufs.yixiu.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;

public class HttpsUtils {

    /**
     * 创建一个 HttpClient，自动支持 TLSv1.2 和 TLSv1.3
     */
    public static CloseableHttpClient createHttpClient() throws Exception {
        // 创建默认 SSLContext，JDK 21 会自动启用 TLSv1.2 和 TLSv1.3
        SSLContext sslContext = SSLContexts.createDefault();

        // 创建 SSL 工厂，允许 TLSv1.2 和 TLSv1.3
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.3", "TLSv1.2"}, // 支持 TLS 1.3 和 1.2
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier()
        );

        // 构建 HttpClient
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
    }

    /**
     * 执行 GET 请求并返回响应内容
     */
    public static String doGet(String url) {
        try (CloseableHttpClient client = createHttpClient()) {
            HttpGet get = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(get)) {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
                } else {
                    return "HTTP error code: " + status;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

