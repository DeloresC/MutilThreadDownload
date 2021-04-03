package com.cui;

import com.cui.util.FileUtils;
import com.cui.util.HttpUtils;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private String url;

    public static final int MAX_THREAD_NUMS = 6;

    private static CountDownLatch count = new CountDownLatch(MAX_THREAD_NUMS);

    private static ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_NUMS);

    public static void main(String[] args) throws IOException, InterruptedException {
        Main main = new Main();
        main.url = "https://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";
        main.download();
    }


    /**
     * 下载文件
     * @throws IOException
     * @throws InterruptedException
     */
    public void download() throws IOException,InterruptedException{
        long fileSize = HttpUtils.getContentLength(url);
        System.out.println("文件大小："+ fileSize);
        if(FileUtils.getLocalFileSize(FileUtils.getFilename(url)) != 0){
            System.out.println("文件已存在.");
            return;
        }
        long startTime = System.currentTimeMillis();
        splitDownloadTask(fileSize);
        count.await();
        long endTime = System.currentTimeMillis();
        System.out.println("本次下载耗时："+(endTime-startTime)/1000);
        executor.shutdown();
        merge(FileUtils.getFilename(url));
    }

    /**
     * 注意：http的range是闭区间，如果是10个字节文件，则rang 0-9 代表这10个字节
     * @param fileSize
     */
    private void splitDownloadTask(long fileSize){
        long size = fileSize / MAX_THREAD_NUMS;
        long lastSize = fileSize - fileSize / MAX_THREAD_NUMS * (MAX_THREAD_NUMS - 1);

        for (int i = 1; i <= MAX_THREAD_NUMS; i++){
            long startPos = (i-1) * size;
            long downloadWindow = (i == MAX_THREAD_NUMS) ? lastSize : size;
            long endPos = startPos + downloadWindow - 1;
            final int part = i;

            executor.execute(() -> {
                String filename = FileUtils.getFilename(url) + ".tmp" + part;
                long localFileSize = FileUtils.getLocalFileSize(filename);

                try (InputStream input = HttpUtils.getHttpURLConnection(url, startPos, endPos).getInputStream();
                     BufferedInputStream bis = new BufferedInputStream(input);
                     RandomAccessFile acFile = new RandomAccessFile(filename, "rw")) {
                    // 定位到上次写入的末尾
                    acFile.seek(localFileSize);
                    byte[] buffer = new byte[1024];
                    int len = -1;
                    while ((len = bis.read(buffer)) != -1){
                        acFile.write(buffer, 0 , len);
                    }
                    System.out.println("part"+part+"下载完毕");
                    count.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    /**
     * 合并临时文件并删除
     * @param fileName
     * @return
     * @throws IOException
     */
    private void merge(String fileName) throws IOException {
        byte[] buffer = new byte[1024];
        int len = -1;
        try (RandomAccessFile oSavedFile = new RandomAccessFile(fileName, "rw")) {
            for (int i = 1; i <= MAX_THREAD_NUMS; i++) {
                String tmpFilename = fileName + ".tmp" + i;
                File tmpFile = new File(tmpFilename);
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tmpFilename))) {
                    while ((len = bis.read(buffer)) != -1) {
                        oSavedFile.write(buffer, 0, len);
                    }
                }
                tmpFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

