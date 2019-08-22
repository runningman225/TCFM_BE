package com.future.tcfm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;

import java.util.List;


public class NotificationEvent extends ApplicationEvent {
    private String type;
    private String groupName;
    private String email;
        public NotificationEvent(Object source, String type, String email, String groupName){
        super(source);
        this.type = type;
        this.email = email;
        this.groupName =groupName;
    }

    public String getType() {
        return type;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getEmail() {
        return email;
    }
}
