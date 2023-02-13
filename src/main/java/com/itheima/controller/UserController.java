package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * ClassName:UserController
 * Package:com.itheima.controller
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/10 - 22:08
 * @Version:v1.0
 */

/**
 * 移动端用户请求
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /*
    *//**
     * 发送手机验证码
     * @param user
     * @param request
     * @return
     *//*
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpServletRequest request){
        //获取手机号
        String phone = user.getPhone();
        if(!StringUtils.isEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("ruigi","SMS_269495445",phone,code);
            //需要将生成的验证码保存到Session
            request.getSession().setAttribute(phone,code);
            return R.success("短信发送成功!");
        }
        return R.error("短信发送失败!");
    }
*/
    /**
     * 移动端用户登录
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpServletRequest request){
        //获取手机号
        String phone = (String) map.get("phone");
        if(!StringUtils.isEmpty(phone)){
            //判断当前登录的用户是否是新用户，
            //如果是新用户就自动完成注册。
            //根据phone查询是否是新用户
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(userLambdaQueryWrapper);
            if(user == null){
                //证明是新用户
                user = new User();
                user.setPhone(phone);
                //保存
                userService.save(user);
            }
            //保存移动端登录的用户id
            request.getSession().setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败!");
    }

    @PostMapping("/loginout")
    public R<String> loginOut(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return R.success("退出成功!");
    }
}
