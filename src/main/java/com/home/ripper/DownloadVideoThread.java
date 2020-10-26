package com.home.ripper;

import com.home.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @ClassName DownloadVideoThread
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/5 17:32
 * @Version 1.0
 */
public class DownloadVideoThread extends Thread {
    private static final Logger logger = LogManager.getLogger(DownloadVideoThread.class);
    private URL url;
    private File saveAs;
    private int retries;
    private URL urlToDownload;
    private final int TIME_OUT;
    private boolean redirected = false;

    public DownloadVideoThread(URL url, File saveAs) {
        this.url = url;
        this.saveAs = saveAs;
        this.retries = 2;
        this.TIME_OUT = 0;
        this.urlToDownload = url;
    }


    @Override
    public void run() {
        int bytesTotal, bytesDownloaded = 0;
        try {
            saveAs = new File(saveAs.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + Utils.sanitizeFileName(saveAs.getName()));
            bytesTotal = getTotalBytes(urlToDownload.toExternalForm());
            logger.debug("文件大小为：" + bytesTotal + "字节");
        } catch (IOException e) {
            logger.error(e.getMessage() + " caused by " + e.getCause());
        }
        if (saveAs.exists()){
            logger.info("[!]删除同名文件，path:" + saveAs);
            saveAs.delete();
        }
        int tries = 0;
        do {
            InputStream is = null;
            OutputStream os = null;
            byte[] data = new byte[1024 * 256];
            int bytesRead;
            try{
                logger.info("正在下载文件：" + urlToDownload + (tries > 0 ? "重新尝试次数：" + tries : ""));
                HttpURLConnection huc;
                if (urlToDownload.toExternalForm().startsWith("https")){
                    huc = (HttpsURLConnection)this.urlToDownload.openConnection();
                }else {
                    huc = (HttpURLConnection)this.urlToDownload.openConnection();
                }
                huc.setInstanceFollowRedirects(true);
                huc.setConnectTimeout(TIME_OUT);
                huc.setRequestProperty("accept", "*/*");
                huc.setRequestProperty("Referer", this.urlToDownload.toExternalForm());
                huc.setRequestProperty("User-agent", Utils.USER_AGENT);
                tries += 1;
                logger.debug("Request properties:" + huc.getRequestProperties());
                huc.connect();

                int statusCode = huc.getResponseCode();
                logger.debug("响应码为：" + statusCode);
                if (statusCode / 100 == 3){
                    if (!redirected){
                        redirected = true;
                        tries--;
                    }
                    String location = huc.getHeaderField("Location");
                    urlToDownload = new URL(location);
                    //丢出异常，程序重新进行连接
                    throw new IOException("Redirected code" + statusCode + "- redirected to " + location);
                }
                if (statusCode / 100 == 4){
                    logger.error("[!]" + "statusCode: " + statusCode + "unable to reach " + urlToDownload);
                    //无法连接到资源，退出
                    return;
                }
                if (statusCode / 100 == 5){
                    //服务器异常，丢出异常，程序重新进行连接
                    throw new IOException("statusCode: " + statusCode + " - server error, retrying......");
                }
                is = new BufferedInputStream(huc.getInputStream());
                os = new FileOutputStream(saveAs);
                while ((bytesRead = is.read(data, 0, data.length)) != -1){
                    os.write(data, 0, bytesRead);
                    bytesDownloaded += bytesRead;
                }
                is.close();
                os.close();
                break;

            }catch (FileNotFoundException fe) {
                logger.error("文件：" + saveAs +"不存在或不能创建。");
            } catch (IOException e) {
                logger.error("下载文件：" + urlToDownload + "时出错" + e.getMessage(),e);
            }finally {
                try {
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (os != null)
                        os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tries > this.retries){
                logger.error("[!]" + "连接(" + urlToDownload + ")超出最大重试次数：" + this.retries);
                return;
            }
        }while (true);
        logger.info("[+] Saved " + urlToDownload + " as " + this.saveAs);
    }

    private int getTotalBytes(String urlToDownload) throws IOException {
        HttpURLConnection connection = null;
        if (urlToDownload.startsWith("https")){
            connection = (HttpsURLConnection)this.url.openConnection();
        }else {
            connection = (HttpURLConnection)this.url.openConnection();
        }
        connection.setRequestMethod("HEAD");
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("User-agent", Utils.USER_AGENT);
        connection.setRequestProperty("Referer", this.urlToDownload.toExternalForm());
        connection.connect();
        return connection.getContentLength();
    }
}
