package com.mycoder.community.service;

import com.mycoder.community.dao.LoginTicketMapper;
import com.mycoder.community.dao.UserMapper;
import com.mycoder.community.entity.LoginTicket;
import com.mycoder.community.entity.User;
import com.mycoder.community.util.CommunityConstant;
import com.mycoder.community.util.CommunityUtil;
import com.mycoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        // process null input
        if(user == null){
            throw new IllegalArgumentException("Input cannot be null");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg:", "username cannot be null");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg:", "password cannot be null");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg:", "email cannot be null");
            return map;
        }

        // check username
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "username has existed");
            return map;
        }

        // check email
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "email has been registered");
            return map;
        }

        // register
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Activate Account", content);

        return map;
    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        // null value
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "username cannot be null!");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "password cannot be null!");
            return map;
        }

        // validate username
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg", "This username does not exist");
            return map;
        }

        // validate status
        if(user.getStatus() == 0){
            map.put("usernameMsg", "This username has not been activated");
            return map;
        }

        // validate password
        password = CommunityUtil.md5(password + user.getSalt());

        //validate password
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg", "incorrect password");
            return map;
        }

        // generate ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl){
        return userMapper.updateHeader(userId, headerUrl);
    }

    public int updatePassword(int userId, String password){
        return userMapper.updatePassword(userId, password);
    }

}
