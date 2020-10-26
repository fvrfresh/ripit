package com.home.ripper.rippers;

import com.home.ripper.AbstractRipper;
import com.home.ripper.DownloadFileThread;
import com.home.utils.Http;
import com.home.utils.Utils;
import com.home.utils.WebPage;
import javafx.animation.FillTransition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName WeiRipper
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/9 15:40
 * @Version 1.0
 */

//缩略图：thumb180  orj360    大图：bmiddle  mw690  登录后主机：d.weibo.com  weibo.com/u/......
public class WeiRipper extends AbstractRipper {

    private static final Logger logger = LogManager.getLogger(WeiRipper.class);

    private static final List<String> WEIBO_HOSTS;
    private Pattern pattern;
    private Matcher matcher;
    static{
        WEIBO_HOSTS = Arrays.asList("weibo.com","s.weibo.com","d.weibo.com");
    }
    public WeiRipper(URL url) throws MalformedURLException {
        super(url);
    }
    @Override
    protected boolean canRip(URL url) {
        String host = url.getHost();
        for (String weiboHost : WEIBO_HOSTS) {
            System.out.println(weiboHost);
            if (weiboHost.equals(host)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void rip() throws IOException {
        int index = 0;
        logger.info("获取url所代表的资源......");
//        Document document = getPage();
//        System.out.println(document);
        WebDriver driver = getDoc();
        /*try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
        }*/

        logger.info("取得url资源，地址：" + driver.getCurrentUrl());

        if (driver != null){
            List<URL> imageUrls = getImageUrlFromPage(driver);
//            List<URL> imageUrls = getImageUrlFromPage(document);
            if (imageUrls.isEmpty()){
                driver.close();
                driver.quit();
                throw new IOException("没有在" + driver.getCurrentUrl() + "找到图片");
            }
            for (URL url: imageUrls) {
                index += 1;
                logger.info("发现图片#" + index + ":" + url.toExternalForm());
                if (AbstractRipper.urls.containsKey(url)){
                    continue;
                }
                downloadUrl(url,index);
            }

            try {
                Thread.sleep(7000);
                threadPool.properShutdown();
                driver.close();
                driver.quit();
            } catch (InterruptedException e) {
                logger.error("Main thread interrupted!!");
            }
        }
    }

    private List<URL> getImageUrlFromPage(WebDriver driver) {
        List<URL> imageUrls;
        logger.info("网页标题为：" + driver.getTitle());
        imageUrls = driver.findElements(By.cssSelector("img"))
                .stream()
                .map(webElement -> webElement.getAttribute("src"))
                .filter(str -> str.startsWith("about") ? false : true)
                .map(spec -> {
                    URL u = null;
                    try {
                        u = new URL(spec);
                    } catch (MalformedURLException e) {

                    }
                    return u;
                })
                .filter(src -> {
                    if (src == null){
                        return false;
                    }
                    String tmp = src.toExternalForm();
                    if (tmp.contains("img.t.sinajs.cn") || tmp.contains("h5.sinaimg.cn")
                            || tmp.contains("about")){
                        return false;
                    }else if (matchesSrc(tmp)){
                        return false;
                    }
                    return true;
                }).filter(url -> !(AbstractRipper.urls.containsKey(url))).collect(Collectors.toList());
        return imageUrls;
    }

    private boolean matchesSrc(String src) {
        Pattern p = Pattern.compile("^.*tvax?[0-9]\\.sinaimg\\.cn.*+$");
        Matcher matcher = p.matcher(src);
        return matcher.matches();
    }

    private WebDriver getDoc() {
        WebPage webPage = new WebPage();
        WebDriver driver = webPage.get(this.url);
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains("https://s.weibo.com/?Refer")){
            String query = currentUrl.substring(currentUrl.indexOf("#"),currentUrl.lastIndexOf("#") + 1);
            try {
                query = URLDecoder.decode(query,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JavascriptExecutor js = (JavascriptExecutor)driver;
            js.executeScript("document.querySelector('div.gn_search_v2 input.W_input').value=" + "'"+ query + "'");
            driver.findElement(By.cssSelector("div.gn_search_v2 a.ficon_search")).click();
        }
        return (WebDriver) driver;
    }

    private void downloadUrl(URL url, int index) {
        Pattern p = Pattern.compile("^.*(thumb150|thumb180|orj360).*",Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(url.toExternalForm());
        if (matcher.matches()){
            System.out.println("matches:" + matcher.matches());
            String[] strings = url.toExternalForm().split("/");
            strings[3] = "mw690";
            try {
                url = new URL(Arrays.stream(strings).collect(Collectors.joining("/")));
            } catch (MalformedURLException e) {
                logger.error("转换url过程中出现错误！");
            }
        }
        String prefix = getPrefix(index);
        downloadUrl(url,prefix);
    }

    private void downloadUrl(URL url, String prefix){
        File saveAs = getFileNameFromUrl(url, prefix);
        downloadUrl(url,saveAs);
    }

    private void downloadUrl(URL url, File saveAs) {
//        AbstractRipper.urls.put(url,saveAs);
        DownloadFileThread downloadFileThread = new DownloadFileThread(url,saveAs);
        threadPool.addThread(downloadFileThread);
    }

    private File getFileNameFromUrl(URL urlToDownload, String prefix) {
        String ext = "";
        String part = Utils.getTitleFromUrl(this.url);
        Pattern p = Pattern.compile("^.*\\.(jpg|jpeg|png|gif|apng|webp|tif|tiff|webm|mp4)$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(urlToDownload.toExternalForm());
//        System.out.println(m.matches());
//        ext = m.group(1);
        ext = urlToDownload.getPath().substring(urlToDownload.getPath().lastIndexOf(".") + 1);
        String savePath = this.workingDir + File.separator+ prefix + part + "." + ext;
//        System.out.println("+++++++++" + savePath);
        File saveAs = new File(savePath);
        return saveAs;
    }

    private String getPrefix(int index) {
        return String.format("%1$03d_",index);
    }

    private List<URL> getImageUrlFromPage(Document document) throws MalformedURLException {
        List<URL> imageUrls = new ArrayList<>();
        for (Element element: document.select("img")) {
            if (!element.hasAttr("src")){
                continue;
            }
            String src = element.attr("src");
            pattern = Pattern.compile("^.*\\.(jpg|jpeg|png|gif|apng|webp|tif|tiff|webm|mp4)$", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(src);
            if (matcher.matches()){
                if (src.startsWith("//")){
                    src = "https:" + src;
                }
                if (src.startsWith("/")){
                    src = "https://" + this.url.getHost() + src;
                }
                if (imageUrls.contains(src) || urls.containsKey(new URL(src))){
                    logger.info("已经下载过该资源");
                    continue;
                }
                imageUrls.add(new URL(src));
            }
        }
        return imageUrls;
    }

    private Document getPage() throws IOException {
        Http http = new Http(this.url);
//        http.referrer(this.url);
        return http.get();
    }

    @Override
    public void setWorkingDir(URL url) throws IOException {
        String dir = Utils.getWorkingDir().getCanonicalPath();
        if (!dir.endsWith(File.separator)){
            dir += File.separator;
        }
        String title;

        title = Utils.getTitleFromUrl(url);
        logger.debug("已取得url的工作目录。");
        if (!title.endsWith(File.separator)){
            title += File.separator;
        }

        dir = dir + title;
        System.out.println("=====" + dir);
        this.workingDir = new File(dir);
        if (!(this.workingDir.exists())){
            logger.info("创建图片的下载目录：" + this.workingDir);
            this.workingDir.mkdirs();
        }
        logger.info("url:" + url + "的工作目录为：" + this.workingDir);
    }

}
