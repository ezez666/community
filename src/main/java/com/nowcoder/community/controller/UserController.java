package com.nowcoder.community.controller;


import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
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
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String updateUser(){
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String updateHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","上传文件不能为空");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(suffix == null){
            model.addAttribute("error","上传文件格式不正确");
            return "/site/setting";
        }
        fileName = CommunityUtil.generateUUID() + suffix;
        File dest = new File(uploadPath+"/"+fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败",e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常",e);
        }
        User user = hostHolder.getUser();
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        userService.updateHeaderUrl(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        fileName = uploadPath + "/" + fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/"+suffix);
        try (FileInputStream fis = new FileInputStream(fileName);
             OutputStream os = response.getOutputStream();
             ){
            byte[] buffer = new byte[1024];
            int n = 0;
            while((n = fis.read(buffer))!=-1){
                os.write(buffer,0,n);
            }
        } catch (Exception e) {
            logger.error("图片读取失败",e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePwd",method = RequestMethod.POST)
    public String updatePwd(String oldPassword,String newPassword,Model model){
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user.getId(),oldPassword,newPassword);
        if(!map.isEmpty()){
            model.addAttribute("oldMessage",map.get("oldMessage"));
            model.addAttribute("newMessage",map.get("newMessage"));
            return "/site/setting";
        }
        return "redirect:/logout";
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

}
