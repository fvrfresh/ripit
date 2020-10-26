package com.home.web;

import com.home.domain.Url;
import com.home.ripper.AbstractRipper;
import com.home.utils.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName RipController
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/7 11:31
 * @Version 1.0
 */
@Controller
@RequestMapping("/test")
public class RipController {

    private WebDriver driver = null;

    @GetMapping
    public String rip() throws Exception {
//        AbstractRipper ripper = AbstractRipper.getRipper(new URL("https://docs.oracle.com/javase/8/docs/api/"));
        System.setProperty("webdriver.gecko.driver","d:/java/MyLib/geckodriver.exe");
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.get("https://weibo.com/");
        Thread.sleep(10000);
        String title = driver.getTitle();
        System.out.println(title);

        driver.findElements(By.cssSelector("img"))
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
                        String tmp = src.toExternalForm();
                        if (tmp.contains("img.t.sinajs.cn") || tmp.contains("h5.sinaimg.cn")
                            || tmp.contains("about")){
                            return false;
                        }else if (matchesSrc(tmp)){
                            return false;
                        }
                        return true;
                    }).filter(url -> !(AbstractRipper.urls.containsKey(url))).forEach(System.out::println);
        return "redirect:/";
    }
    private boolean matchesSrc(String src) {
        Pattern p = Pattern.compile("^.*tvax[0-9]\\.sinaimg\\.cn.*+$");
        Matcher matcher = p.matcher(src);
        return matcher.matches();
    }
}
