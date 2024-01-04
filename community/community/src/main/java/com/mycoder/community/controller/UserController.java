package com.mycoder.community.controller;


import com.mycoder.community.annotation.LoginRequired;
import com.mycoder.community.entity.User;
import com.mycoder.community.service.UserService;
import com.mycoder.community.util.CommunityUtil;
import com.mycoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error", "Have not chose an image!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "Invalid file form!");
            return "/site/setting";
        }

        // Generate random file name
        fileName = CommunityUtil.generateUUID() + suffix;
        // check save path
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // save file
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("fail to upload file: " + e.getMessage());
            throw new RuntimeException("fail to upload file, server error!" + e.getMessage());
        }

        // update headerurl for current user(web path)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // Server save file path
        fileName = uploadPath + "/" + fileName;
        // file suffix
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // response file
        response.setContentType("image/" + suffix);
        OutputStream os = null;
        FileInputStream fis = null;
        try {
            os = response.getOutputStream();
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("Fail to get header: " + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e){
                logger.error("Fail to close stream: " + e.getMessage());
            }
        }
    }

    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String originalPassword, String newPassword, String confirmPassword, Model model,
                                 @CookieValue("ticket") String ticket){
        if(originalPassword == null){
            model.addAttribute("originalPasswordMsg", "please input the original password");
            System.out.println("1");
            return "site/setting";
        }

        if(newPassword == null){
            model.addAttribute("newPasswordMsg", "please input the new password");
            System.out.println("2");
            return "site/setting";
        }

        if(confirmPassword == null){
            model.addAttribute("confirmPasswordMsg", "please input the new password");
            System.out.println("3");
            return "site/setting";
        }

        User user = hostHolder.getUser();
        if(!CommunityUtil.md5(originalPassword + user.getSalt()).equals(user.getPassword())){
            model.addAttribute("originalPasswordMsg", "wrong original password");
            System.out.println("4");
            return "site/setting";
        }

        if(!confirmPassword.equals(newPassword)){
            model.addAttribute("confirmPasswordMsg", "Entered passwords are not same");
            System.out.println("5");
            return "site/setting";
        }
        userService.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));
        userService.logout(ticket);
        return "redirect:/login";
    }

}
