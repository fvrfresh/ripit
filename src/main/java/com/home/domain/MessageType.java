package com.home.domain;

public enum MessageType {
    ERROR("Error"),INFO("Info"),WARNING("Warning");
    String type;
    MessageType(String value){
        this.type = value;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
