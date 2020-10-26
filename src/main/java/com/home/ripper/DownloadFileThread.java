package com.home.ripper;

import com.home.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * @ClassName DownloadFileThread
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/5 17:29
 * @Version 1.0
 */
public class DownloadFileThread extends Thread {
    private static final Logger logger = LogManager.getLogger(DownloadFileThread.class);


    private URL url;
    private File saveAs;
    private int retries;
    private final int TIMEOUT;
    private String prettySaveAs;
    private String referrer = "";

    public void setReferrer(String referrer){
        this.referrer = referrer;
    }

    public DownloadFileThread(URL url, File saveAs) {
        super();
        this.url = url;
        this.saveAs = saveAs;
        this.prettySaveAs = Utils.getPrettyName(saveAs);
        this.retries = 2;
        this.TIMEOUT = 6000;
    }

    @Override
    public void run() {
        //确保saveAs的文件名不包含违规字符
        try {
            saveAs = new File(saveAs.getParentFile().getCanonicalPath() + File.separator + Utils.sanitizeFileName(saveAs.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        long fileSize = 0;
        int byteTotal = 0;
        int byteDownloaded = 0;
        if (saveAs.exists()){
            logger.info(saveAs.getName() + "文件已存在，" + "删除该文件！！");
            saveAs.delete();
        }
        URL urlToDownload = this.url;
        boolean redirected = false;
        int tries = 0;
        do {
            tries += 1;
            InputStream is = null;
            OutputStream os = null;
            try{
                logger.info("下载文件：" + urlToDownload + "\n" + (tries > 0 ? "Retries #" + tries : ""));
                //配置urlConnection
                HttpURLConnection huc;
                if(this.url.toString().startsWith("https")){
                    huc = (HttpsURLConnection)urlToDownload.openConnection();
                }else {
                    huc = (HttpURLConnection)urlToDownload.openConnection();
                }
                huc.setInstanceFollowRedirects(true);
                huc.setConnectTimeout(TIMEOUT);
                huc.setReadTimeout(TIMEOUT);
                if (!referrer.equals("")){
                    huc.setRequestProperty("Referer", referrer);
                }
                huc.setRequestProperty("accept", "*/*");
                huc.setRequestProperty("User-agent", Utils.USER_AGENT);
                logger.debug("请求头信息：" + huc.getRequestProperties());
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
                    logger.error("[!]" + "statusCode: " + statusCode + "unable to reach " + url);
                    //无法连接到资源，退出
                    return;
                }
                if (statusCode / 100 == 5){
                    //服务器异常，丢出异常，程序重新进行连接
                    throw new IOException("statusCode: " + statusCode + " - server error, retrying......");
                }
                byteTotal = huc.getContentLength();
                //从httpUrlConnection中保存输入流，从而获取文件
                is = new BufferedInputStream(huc.getInputStream());
                logger.info(is.available());
                try{
                    os = new FileOutputStream(saveAs);
                }catch (FileNotFoundException fne){
                    if (saveAs.getAbsolutePath().length() > 259 && Utils.isWindows()){
                        File f = Utils.shortenSaveAsWindows(saveAs.getParentFile().getPath(), saveAs.getName());
                        os = new FileOutputStream(f);
                        saveAs =f;
                    }
                }
                byte[] data = new  byte[1024 * 256];
                int bytesRead = 0;
                while ((bytesRead = is.read(data, 0, data.length)) != -1){
                    os.write(data, 0, bytesRead);
                }
                is.close();
                os.close();
                if (!(AbstractRipper.urls.containsKey(url.toExternalForm()))){

                    AbstractRipper.urls.put(url.toExternalForm(),saveAs);
                }
                break;

            }catch (SocketTimeoutException timeoutException){
                logger.error("[!]" + url.toExternalForm() + "超时");
                break;
            }catch (MalformedURLException me){
                logger.error("重定向url失败" + "原因：" + me.getCause());
                break;
            }catch (IOException ioe){
                logger.debug("IOExcpetion", ioe);
                logger.error("[!]" + "下载" + url + "时出错" + "-" + ioe.getMessage());
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
                logger.error("[!]" + "连接(" + url + ")超出最大重试次数：" + this.retries);
                return;
            }
        }while(true);
        logger.info("[+] Saved " + url + " as " + this.saveAs);
    }
}
