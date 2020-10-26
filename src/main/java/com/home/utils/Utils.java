package com.home.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.domain.ImageInfo;
import com.home.domain.Url;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName Utils
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/5 17:14
 * @Version 1.0
 */
public class Utils {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String RIP_DIRECTORY = "rips";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:78.0) Gecko/20100101 Firefox/78.0";
    public static final List<URL> urls = new ArrayList<URL>();
    private static Logger logger = LogManager.getLogger(Utils.class);
    public static String sanitizeFileName(String fileName){
        return fileName.replaceAll("[\\\\/:*?\":<>|]", "_");
    }

    public static String getPrettyName(File saveAs) {
        String prettyName = saveAs.toString();
        try {
            prettyName = saveAs.getCanonicalPath();
            String cwd = new File(".").getCanonicalPath() + File.separator;
            prettyName = prettyName.replace(cwd, "." + File.separator);
        } catch (IOException e) {
            logger.error("错误：" + e.getMessage() + "\n原因：" + e.getCause());
        }
        return prettyName;

    }

    public static boolean isWindows() {
        return OS.startsWith("win");
    }

    public static File shortenSaveAsWindows(String path, String name) throws FileNotFoundException {
        logger.error("文件名 " + name + " 太长，无法与文件系统兼容。 ");
        logger.info("Shorting fileName......");
        String fullPath = path + File.separator + name;
        int pathLength = path.length();
        int fileNameLength = name.length();
        if (pathLength == 260){
            throw new FileNotFoundException("文件路径过长，超出操作系统限制。");
        }
        String[] fileNameParts = name.split("\\.");
        String ext = fileNameParts[fileNameParts.length - 1];
        fullPath = fullPath.substring(0, 259 - pathLength - ext.length() + 1) + "." + ext;
        return new File(fullPath);
    }

    public static int getAvailableCores(){
        return Runtime.getRuntime().availableProcessors();
    }

    public static String getUrlString(String string){
        if (string != null && string != ""){
            String[] strings = string.split("=");
            String tmpUrl = Arrays.stream(strings)
                    .map(Utils::decodeUrl)
                    .skip(1L)
                    .collect(Collectors.joining());
            return tmpUrl;
        }else {
            return "Null";
        }
    }

    public static String getJsonString(String rawStr){
        Url url = new Url();
        String tmpUrl = Utils.getUrlString(rawStr);
        url.setRawUrl(tmpUrl);
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = "";
        try {
            jsonStr = mapper.writeValueAsString(url);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e.getCause());
        }
        System.out.println(jsonStr);
        return jsonStr;
    }

    private static String decodeUrl(String string){
        String tmpStr = string;
        try {
            tmpStr = URLDecoder.decode(URLDecoder.decode(string, "UTF-8"),"utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("无法解码经编码的url！");
        }
        return tmpStr;
    }

    public static File getWorkingDir() {
        String workingDir = "";
        try {
            workingDir = new File("images").getCanonicalPath() + File.separator + Utils.RIP_DIRECTORY + File.separator;
        } catch (IOException e) {
            logger.error("设置工作目录时出现错误");
        }
        File f = new File(workingDir);
        if (!f.exists()){
            f.mkdirs();
        }
        return f;
    }

    public static String getTitleFromUrl(URL url) {

        String urlStr = url.toExternalForm().trim();
        if (!(urlStr.isEmpty()) && !(urlStr == null)) {

            Pattern p = Pattern.compile("https?://weibo\\.com/(u/)?(\\d{9,})/?.*+",Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(urlStr);
            if (matcher.matches()){
                String user = matcher.group(2);
                return "uid_" + user;
            }

            Pattern p2 = Pattern.compile("https?://weibo\\.com/([a-zA-Z]+)\\??.*+", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = p2.matcher(urlStr);
            if (matcher2.matches()){
                String user = matcher2.group(1);
                return "uname_" + user;
            }

            //匹配转义后的url，并取出十六进制字符串
            /*Pattern p = Pattern.compile("https:?//s\\.weibo\\.com/weibo/((%\\p{XDigit}+)*+)\\??.*+", Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(u.toExternalForm());
            System.out.println(p);
            if (matcher.matches()){
                System.out.println(matcher.group(0));
                String user = matcher.group(1);
            }*/
            Pattern p3 = Pattern.compile("https:?//s\\.weibo\\.com/weibo/?\\??q?=?#?([\u4e00-\u9fa5_a-zA-Z0-9]*)#?\\?*.*+", Pattern.CASE_INSENSITIVE);
            Matcher matcher3 = p3.matcher(urlStr);
            if (matcher3.matches()){
                String user = matcher3.group(1).isEmpty() ? matcher3.group(0) : matcher3.group(1);;
                return "search_" + user;
            }
            Pattern p0 = Pattern.compile("https://(d\\.)?weibo.com/(\\S*+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = p0.matcher(urlStr);
            if (matcher.matches()) {
                if (matcher.group(1) == null) {
                    return "general_site";
                }
                return "general_" + matcher.group(1).substring(0, 1);
            }

        }
        return "default_dir";
    }

    public static String getJsonString(Map<String, File> urls) throws JsonProcessingException {
        List<ImageInfo> imageInfos = new ArrayList<>();

        Set<String> keySet = urls.keySet();
        keySet.stream().forEach(new Consumer<String>() {
            @Override
            public void accept(String url) {
                ImageInfo img = new ImageInfo();
                img.setImgUrl(url);
                try {
                    img.setSaveFile(urls.get(url).getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageInfos.add(img);
            }
        });
        ObjectMapper mapper = new ObjectMapper();
        String string = mapper.writeValueAsString(imageInfos);
        return string;

    }
}
