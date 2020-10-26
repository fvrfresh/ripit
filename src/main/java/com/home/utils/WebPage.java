package com.home.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName WebPage
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/20 15:54
 * @Version 1.0
 */

public class WebPage {
    private static final Logger logger = LogManager.getLogger(WebPage.class);
    private FirefoxOptions options;
    private WebDriver driver;
    private static final String DRIVERPATH;
    private static final String DRIVERNAME;
    private int TIME_WAIT;
    private int THREAD_SLEEPING;
    static{
        DRIVERNAME = "webdriver.gecko.driver";
        DRIVERPATH = "d:/java/MyLib/geckodriver.exe";
        logger.info("配置WebDriver第三方驱动......");
        System.setProperty(DRIVERNAME,DRIVERPATH);
    }
    public WebPage(){
        options = new FirefoxOptions();
        options.setHeadless(true);
        driver = new FirefoxDriver(options);
        TIME_WAIT = 15;
        THREAD_SLEEPING = 7000;
        driver.manage().timeouts().implicitlyWait(TIME_WAIT, TimeUnit.SECONDS);

    }

    public WebDriver get(URL url){
        logger.info("访问网络资源，url：" + url);
        driver.get(url.toExternalForm());
        try {
            Thread.sleep(THREAD_SLEEPING);
        } catch (InterruptedException e) {
            logger.error("线程在休眠过程中被打断");
        }
        return driver;
    }


    public static String getDRIVERPATH() {
        return DRIVERPATH;
    }

    public static String getDRIVERNAME() {
        return DRIVERNAME;
    }

    public int getTIME_WAIT() {
        return TIME_WAIT;
    }


    public int getTHREAD_SLEEPING() {
        return THREAD_SLEEPING;
    }

}
