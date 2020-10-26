package com.home.domain;

import java.util.Objects;

/**
 * @ClassName ErrorJsonMessage
 * @Description TODO
 * @Author zhang
 * @Date 2020/7/9 19:28
 * @Version 1.0
 */
public class JsonMessage {
    public String message;
    public final Enum TYPE;

    public JsonMessage(String message, Enum ERROR) {
        this.message = message;
        this.TYPE = ERROR;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }


    public Enum getERROR() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "JsonMessage{" +
                "message='" + message + '\'' +
                ", TYPE=" + TYPE +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonMessage that = (JsonMessage) o;
        return message.equals(that.message) &&
                TYPE.equals(that.TYPE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, TYPE);
    }
}
