package com.mycoder.community.controller.interceptor;

import com.mycoder.community.entity.LoginTicket;
import com.mycoder.community.entity.User;
import com.mycoder.community.service.UserService;
import com.mycoder.community.util.CookieUtil;
import com.mycoder.community.util.HostHolder;
import com.sun.xml.internal.ws.api.ha.StickyFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get ticket from cookie
        String ticket = CookieUtil.getValue(request, "ticket");

        if(ticket != null){
            // search ticket
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // check ticket is valid or not
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                // Get user by ticket
                User user = userService.findUserById(loginTicket.getUserId());
                // Carry user in this request
                hostHolder.setUser(user);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
