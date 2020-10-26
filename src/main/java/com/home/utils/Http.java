package com.home.utils;

import jdk.nashorn.api.scripting.ScriptUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName Http
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/13 17:28
 * @Version 1.0
 */
public class Http {

    private static final int TIME_OUT = 5 * 1000;
    private static final Logger logger = LogManager.getLogger(Http.class);
    private int retries;
    private Connection connection;
    private String url;

    public Http(URL url){
        this.url = url.toExternalForm();
        defaultSettings();
    }
    public Http(String url){
        this.url = url;
        defaultSettings();
    }

    private void defaultSettings() {
        this.retries = 2;
        connection = Jsoup.connect(url);
        connection.timeout(Http.TIME_OUT);
        connection.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        connection.header("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        connection.header("Accept-Encoding","gzip, deflate, br");
        connection.header("TE","Trailers");
        connection.header("Upgrade-Insecure-Requests","1");
        connection.header("Connection","keep-alive");
//        connection.header("If-Modified-Since","Fri, 17 Jul 2020 12:58:02 GMT");
        connection.header("Upgrade-Insecure-Requests","1");
        connection.header("Origin","https://weibo.com");

        connection.cookies(getCookies());
        connection.maxBodySize(0);
        connection.followRedirects(true);
        connection.method(Connection.Method.GET);
        connection.userAgent(Utils.USER_AGENT);
    }

    private Map<String, String> getCookies() {

        Map<String,String> cookies = new HashMap<>();
        cookies.put("Apache", "5555623973022.7295.1594984406974");
        cookies.put("cross_origin_proto","SSL");
        cookies.put("login_sid_t","61a7ae63f8eb6077cccb08f73a4838df");
        cookies.put("SINAGLOBAL","5555623973022.7295.1594984406974");
        cookies.put("SUB","_2AkMoTQjif8NxqwJRmP4XyGPjZYxxzw7EieKeEfk5JRMxHRl-yT9jqhY6tRB6A80mDZrZ5ZSCrTnVoqVevKz8z1rgqTov");
        cookies.put("SUBP","0033WrSXqPxfM72-Ws9jqgMF55529P9D9WhoRC8DA-JiL2kuYAjfwSrw");
        cookies.put("TC-V5-G0","595b7637c272b28fccec3e9d529f251a");
        cookies.put("Ugrow-G0","5c7144e56a57a456abed1d1511ad79e8");
        cookies.put("ULV","1594984407007:1:1:1:5555623973022.7295.1594984406974:");
        cookies.put("wb_view_log","1536*8641.25");
        cookies.put("WBStorage","42212210b087ca50|undefined");
        cookies.put("_s_tentry", "-");
        return cookies;
    }

    public static Http url(URL url){
        return new Http(url);
    }
    public static Http url(String url){
        return new Http(url);
    }

    public Http timeOut(int timeOut){
        connection.timeout(timeOut);
        return this;
    }

    public Http referrer(URL ref){
        connection.referrer(ref.toExternalForm());
        return this;
    }
    public Http referrer(String ref){
        connection.referrer(ref);
        return this;
    }
    public Http userAgent(String userAgent){
        connection.userAgent(userAgent);
        return this;
    }
    public Http retries(int retries){
        this.retries = retries;
        return this;
    }
    public Http header(String header, String value){
        connection.header(header,value);
        return this;
    }
    public Http method(Connection.Method method){
        connection.method(method);
        return this;
    }

    public Connection connection(){
        return this.connection;
    }
    public Document get() throws IOException {

        Document document = response().parse();
//        Document document = connection.get();
        return document;
    }
    public Document post() throws IOException {
        connection.method(Connection.Method.POST);
        return response().parse();
    }

    private Connection.Response response() throws IOException {
        int retries = this.retries;
        Connection.Response response;
        while(--retries >= 0){
            try {
                response = connection().execute();
                return response;
            } catch (IOException e) {
                if (retries != 0){
                    logger.error("无法获取" + this.url + "的资源，重新尝试1次......");
                }
            }
        }
        throw new IOException("无法获取" + this.url +"的资源");
    }

}
