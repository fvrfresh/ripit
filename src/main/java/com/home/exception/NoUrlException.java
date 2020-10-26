package com.home.exception;

/**
 * @ClassName NoUrlException
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/10 14:26
 * @Version 1.0
 */
public class NoUrlException extends Exception {

    public NoUrlException() {
        super();
    }
    public NoUrlException(String message){
        super(message);
    }

    public NoUrlException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoUrlException(Throwable cause) {
        super(cause);
    }

    protected NoUrlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        return "No More Url Exception!";
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }
}
