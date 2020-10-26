package com.home.domain;

/**
 * @ClassName Url
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/7 12:24
 * @Version 1.0
 */
public class ImageInfo {
    private String imgUrl;
    private String saveFile;

    public ImageInfo(String imgUrl, String saveFile) {
        this.imgUrl = imgUrl;
        this.saveFile = saveFile;
    }

    public ImageInfo() {
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(String saveFile) {
        this.saveFile = saveFile;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "imgUrl='" + imgUrl + '\'' +
                ", saveFile='" + saveFile + '\'' +
                '}';
    }
}
