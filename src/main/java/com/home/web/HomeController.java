package com.home.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.domain.JsonMessage;
import com.home.domain.MessageType;
import com.home.exception.NoUrlException;
import com.home.ripper.AbstractRipper;
import com.home.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import sun.plugin2.message.Message;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @ClassName HomeController
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/7 14:02
 * @Version 1.0
 */
@RestController
@RequestMapping(path = "/index", produces = "application/json")
@CrossOrigin(origins = "*")
public class HomeController {
    private static final Logger logger = LogManager.getLogger(HomeController.class);
    private AbstractRipper ripper = null;
    public boolean isRipping = false;
    @PostMapping
    public String processUrl(@RequestBody String data){
        logger.info("取得Url字符串。");
        data = data.trim();
        URL u = null;
        String url = Utils.getUrlString(data);
        if (!url.startsWith("https:")){
            url = "https:" + url;
        }
        try {
            u = new URL(url);
            System.out.println(u);
        } catch (MalformedURLException e) {
            logger.error("URL格式不正确", e.getCause());
//            isRipping = false;
            return getErrorMessage(u.toExternalForm(),"URL格式不正确");
        }

        if (!Utils.urls.contains(u)){
            logger.info("将url放入url池中");
            Utils.urls.add(u);
        }
//        if (!isRipping){
            try {
                ripNextUrl();
            }catch (NoUrlException ne){
                logger.info("url池已无url");
                isRipping = false;
                return getInfoMessage();
            }
            catch (Exception e) {
                logger.error("无法找到适合" + u.toExternalForm() + "的ripper", e);
                isRipping = false;
                return getErrorMessage(u.toExternalForm(),"无法为url找到合适的ripper");
            }
//        }
        String jsonStr = null;

        try {
            Thread.sleep(25000);
            jsonStr = Utils.getJsonString(AbstractRipper.urls);
            AbstractRipper.urls.clear();
        } catch (JsonProcessingException e) {
            return getErrorMessage(url,"将图片url转化成Json字符串时出现错误");
        } catch (InterruptedException e) {
            return getErrorMessage(url,"进程睡眠过程中被打断");
        }
//        String jsonStr = Utils.getJsonString(data);

        return jsonStr;
    }

    private void ripNextUrl() throws Exception {
//        isRipping = true;
        if (Utils.urls.isEmpty()){
//            isRipping = false;
            logger.info("url池为空");
            throw new NoUrlException("url池为空");
        }
        URL nextUrl = Utils.urls.remove(0);
        Thread t = ripUrl(nextUrl);
        if (t == null){
//            isRipping=false;
            ripNextUrl();
        }else {
            t.start();
        }
    }

    private Thread ripUrl(URL nextUrl) throws Exception {
        boolean failed = false;
        ripper = AbstractRipper.getRipper(nextUrl);
        ripper.setUp();
        if (!failed){
            try{
                Thread t = new Thread(ripper);
                return t;
            } catch (Exception e) {
                logger.error("处理url时出错：" + e.getMessage(), e);
//                isRipping = false;
            }
        }
        return null;
    }
    private String getInfoMessage() {
        JsonMessage infoJsonMessage = new JsonMessage("url池已无url", MessageType.INFO);
        return writeValue(infoJsonMessage);
    }

    private String getErrorMessage(String url, String message) {
        JsonMessage errorJsonMessage = new JsonMessage(message, MessageType.ERROR);
        return writeValue(errorJsonMessage);
    }
    private String writeValue(JsonMessage message){
        ObjectMapper mapper = new ObjectMapper();
        String string = "";
        try {
            string = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.error("生成消息时出现错误");
        }
        return string;
    }
}
