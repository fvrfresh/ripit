package com.home.web;


import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

/**
 * @ClassName ImageController
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/22 9:28
 * @Version 1.0
 */
@RestController
@RequestMapping(path = "/images/rips/*")
@CrossOrigin(origins = "*")
public class ImageController {

   /* @PostMapping
    public String deleteSrc(HttpServletRequest request) throws InterruptedException {
        Thread.sleep(5000);
        String path = request.getRequestURI();
        String cmd = request.getParameter("CMD");
        if (cmd.equals("DELETE_SRC")){
            String pathName = "D:/IdeaProject/ripit" + path;
            File f = new File(pathName);
            if (f.exists()){
                File[] files = f.listFiles();
                for (File file : files) {
                    if (file.isFile()){
                        file.delete();
                    }
                    else {
                        File[] files1 = file.listFiles();
                        if (files1.length > 0) {
                            for (File listFile : files1) {
                                listFile.delete();
                            }
                            file.delete();
                        }else {
                            file.delete();
                        }
                    }
                }
            }
        }
        return "delete src success";
    }*/

    @GetMapping("/{path}/{fileName}")
    public String getImage(@PathVariable(name = "path") String path,
                           @PathVariable(name ="fileName") String fileName){
        String pathName = "D:/IdeaProject/ripit/images/rips/" + path + File.separator + fileName;
        String base64String = encodeBase64Image(pathName);

        return base64String;
    }

    private final String encodeBase64Image(String pathName) {
        File f = new File(pathName);
        byte[] content = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(f);
            content = new byte[(int)f.length()];
            fileInputStream.read(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode(content));

    }
}
