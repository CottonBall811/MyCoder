package com.mycoder.community.util;

import com.mycoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * hold user info, instead session object
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
