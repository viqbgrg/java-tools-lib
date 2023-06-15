package com.github.viqbgrg.tools.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadHelper {
    public static void download(String url, String path) throws IOException {
        System.out.println("下载中..." + path);
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(10 * 1000);
            File file = new File(path);
            //文件夹是否存在
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();


            int responseCode = urlConnection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = urlConnection.getInputStream();
                int len = 0;
                byte[] data = new byte[4096];
                //用于保存当前进度（具体进度）
                int progres = 0;
                //获取文件长度
                int maxProgres = urlConnection.getContentLength();
                if (file.exists()) {
                    if (file.length() == maxProgres) {
                        return;
                    }
                    file.delete();
                    file.createNewFile();
                }
                randomAccessFile = new RandomAccessFile(file, "rwd");
                //设置文件大小
                randomAccessFile.setLength(maxProgres);
                //将文件大小分成100分，每一分的大小为unit
                int unit = maxProgres / 100;
                //用于保存当前进度(1~100%)
                int unitProgress = 0;
                while (-1 != (len = inputStream.read(data))) {
                    randomAccessFile.write(data, 0, len);
                    progres += len;//保存当前具体进度
                    int temp = progres / unit; //计算当前百分比进度
                    if (temp >= 1 && temp > unitProgress) {//如果下载过程出现百分比变化
                        unitProgress = temp;//保存当前百分比
                        System.out.println("正在下载中..." + unitProgress + "%: " + path);
                    }
                }
                inputStream.close();
                System.out.println("下载完成...");
            } else {
                System.out.println("服务器异常..." + path);
            }
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
            if (null != randomAccessFile) {
                randomAccessFile.close();
            }
        }

    }
}