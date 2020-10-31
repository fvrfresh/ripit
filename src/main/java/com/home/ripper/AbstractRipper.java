package com.home.ripper;

import com.home.web.HomeController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName AbstractRipper
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/8 18:46
 * @Version 1.0
 */
public abstract class AbstractRipper implements Runnable{
    private static final Logger logger = LogManager.getLogger(AbstractRipper.class);
    protected static boolean onlyUrls = true;
    public static Map<String, File> urls = Collections.synchronizedMap(new HashMap<>());
    protected DownloadThreadPool threadPool;
    protected URL url = null;
    protected File workingDir;
    public abstract void setWorkingDir(URL url) throws IOException;
    protected abstract boolean canRip(URL url);

    public AbstractRipper(){}

    public AbstractRipper(URL url) throws MalformedURLException {
        if (!canRip(url)){
            throw new MalformedURLException("url格式不符合规范：" + url);
        }
        this.url = url;
    }


    public static AbstractRipper getRipper(URL nextUrl) throws Exception {
        for (Constructor<AbstractRipper> constructor : getAllRippersConstructors()) {
            try{
                AbstractRipper ripper = constructor.newInstance(nextUrl);
                logger.info("发现可用的ripper：" + ripper.getClass().getName());
                return ripper;
            }catch (Exception e){

            }
        }
        throw new Exception("未找到匹配的ripper");
    }
    public static boolean testPath(Path path, BasicFileAttributes attributes){
        return path.toAbsolutePath().toString().endsWith(".class")
                && path.toAbsolutePath().toString().contains("rippers");
    }

    private static List<Constructor<AbstractRipper>> getAllRippersConstructors() {
        String codeSource = AbstractRipper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        Pattern pattern = Pattern.compile("^file:/(.*[.]jar)[!].*");
        Matcher matcher = pattern.matcher(codeSource);
        File f = new File(".");
        List<Constructor<AbstractRipper>> constructors = new ArrayList<>();
        try {
            if (matcher.matches()){
                codeSource = matcher.group(1);
                JarFile jarFile = new JarFile(codeSource);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()){
                    JarEntry nextElement = entries.nextElement();
                    String entryName = nextElement.getName();
                    if (!nextElement.isDirectory() && entryName.indexOf("com") > 0 && entryName.contains("rippers") && entryName.endsWith(".class")) {
                        String className = entryName.substring(entryName.indexOf("com")).replace('/', '.').replace('\\', '.').replace(".class", "");
                        try {
                            Constructor<AbstractRipper> ripperConstructor = null;
                            Class<AbstractRipper> ripperClass = (Class<AbstractRipper>) Class.forName(className);
                            if (AbstractRipper.class.isAssignableFrom(ripperClass)){
                                ripperConstructor = ripperClass.getConstructor(URL.class);
                            }
                            constructors.add(ripperConstructor);
                        } catch (ClassNotFoundException e) {
                            logger.error("ClassNotFoundException loading " + className);
                            jarFile.close();
                            throw new RuntimeException("ClassNotFoundException loading " + className);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return constructors;
            }
            Path path = FileSystems.getDefault().getPath(f.getCanonicalPath());
            Stream<Path> list = Files.find(path,Integer.MAX_VALUE,AbstractRipper::testPath);
            constructors = list.map(Path::toString)
                .map(s -> s.substring(s.indexOf("com"), s.lastIndexOf(".")))
                .map(s -> s.replace("\\", "."))
                .map(s -> {
                    Constructor<AbstractRipper> ripperClassConstructor = null;
                    try {
                        Class<AbstractRipper> ripperClass = (Class<AbstractRipper>) Class.forName(s);
                        if (AbstractRipper.class.isAssignableFrom(ripperClass)){
                            ripperClassConstructor = ripperClass.getConstructor(URL.class);
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        logger.error("获取ripper的构造器时出现错误：" + e.getCause());
                    }
                    return ripperClassConstructor;
                })
                .collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return constructors;

    }

    @Override
    public void run() {
        try{
            rip();
        }catch (IOException ioe){
            logger.error("ripper在运行过程中出现错误：" + ioe.getMessage());
            threadPool.properShutdown();
        } finally {
            cleanUp();
        }
    }

    protected void cleanUp(){
        if (this.workingDir.list().length == 0){
            logger.info("删除空文件目录" + this.workingDir);
            boolean result = this.workingDir.delete();
            if (!result){
                logger.error("删除文件目录：" + this.workingDir + "时出错");
            }
            threadPool.properShutdown();
        }
    }

    protected abstract void rip() throws IOException;

    public void setUp() throws IOException {
        setWorkingDir(this.url);
        this.threadPool = new DownloadThreadPool();
    }

}
