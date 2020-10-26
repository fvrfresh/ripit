package com.home.domain;

/**
 * @ClassName Url
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/15 22:21
 * @Version 1.0
 */
public class Url {
    private String rawUrl;

    public Url(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    public Url(){}
    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    @Override
    public String toString() {
        return "Url{" +
                "rawUrl='" + rawUrl + '\'' +
                '}';
    }
}
