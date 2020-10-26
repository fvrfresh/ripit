package com.home.ripper;

import com.home.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.rmi.CORBA.Util;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName DownloadThreadPool
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/5 17:38
 * @Version 1.0
 */
public class DownloadThreadPool {
    private static final Logger logger = LogManager.getLogger(DownloadThreadPool.class);
    public ExecutorService executor = null;

    public DownloadThreadPool(){
        initialize("Main");
    }
    public DownloadThreadPool(String poolName){
        initialize(poolName);
    }


    private void initialize(String poolName) {
        logger.info("启动线程池：" + poolName + ", 最大线程数为：" + Utils.getAvailableCores() / 2);
        executor = Executors.newWorkStealingPool(Utils.getAvailableCores() / 2);
    }

    /**
     * 在线程池中执行t
     * @param t
     */
    public void addThread(Runnable t){
        executor.execute(t);
    }

    /**
     * 关闭线程池
     */
    public void properShutdown(){
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("等待线程池关闭的过程中受到干扰：", e.getCause());
        }
    }
}
