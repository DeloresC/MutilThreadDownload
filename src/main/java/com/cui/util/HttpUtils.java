package com.cui.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    /**
     * 获取网络文件的大小
     * @param url
     * @return
     * @throws IOException
     */
    public static int getContentLength(String url) throws IOException {
        URL fileUrl = new URL(url);
        int contentLength = fileUrl.openConnection().getContentLength();
        return contentLength;
    }

    /**
     * 获取区间下载的连接
     * @param url
     * @param start
     * @param end
     * @return
     * @throws IOException
     */
    public static HttpURLConnection getHttpURLConnection(String url, long start, long end) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("RANGE", "bytes=" + start + "-" + end);
        return connection;
    }

}
