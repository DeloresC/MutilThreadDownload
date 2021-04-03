package com.cui.util;

import java.io.File;

public class FileUtils {

    /**
     * 根据url设置文件名
     * @param url
     * @return
     */
    static public String getFilename(String url){
        return url.substring(url.lastIndexOf("/") + 1);
    }


    /**
     * 获取本地文件大小，如果存在
     * @param filename
     * @return
     */
    public static long getLocalFileSize(String filename){
        File file = new File(filename);
        return file.exists() ? file.length() : 0;
    }

}
