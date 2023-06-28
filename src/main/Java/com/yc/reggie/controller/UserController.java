package com.yc.reggie.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yc.reggie.common.CustomException;
import com.yc.reggie.common.R;
import com.yc.reggie.entity.User;
import com.yc.reggie.service.UserService;
import com.yc.reggie.utils.SMSUtils;
import com.yc.reggie.utils.ValidateCodeUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取验证码，并把生成的验证码加入到Session方便后续比对
     * 
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> getMsg(@RequestBody User user, HttpSession session) {

        String phone = user.getPhone();

        // 阿里云api，发送验证码
        // SMSUtils.sendMessage("瑞吉外卖", "你已收到验证码${code},请妥善保管", user.getPhone(), null);

        // 使随机生成验证码
        if (phone != null) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info(code);
            // 将生成的验证码保存到session，后面可以用手机号取出
            session.setAttribute(phone, code);
            return R.success("发送验证码成功！");
        }

        return R.error("短信发送失败");
    }

    /***
     * 当用户登陆时，发请求会带着phone和验证码
     * 而user中没有验证码属性
     * 可以有Map接受键值对
     * phone->13xxxxxxxxx
     * code->xxxx
     * 
     * @param Map
     * @param request
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {

        log.info("接收到用户输入的验证码：{}", map.get("code"));
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        // 当时存放session时使用phone变量存的
        log.info("接收到后端生成验证码:{}", session.getAttribute(phone));
        String genCode = session.getAttribute(phone).toString();

        // 当用户输入的验证码不为空且正确时
        if (code == null) {
            throw new CustomException("验证码不能为空");
        } else if (code.equals(genCode)) {
            // 若在数据库中未找到该用户，则自动注册
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(User::getPhone, phone);
            if (userService.getOne(userQueryWrapper) == null) {
                User user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);

            }
            // 在session中绑定用户id才能记住登录状态
            User one = userService.getOne(userQueryWrapper);
            session.setAttribute("user", one.getId());
            return R.success(one);
        }

        return R.error("登录失败");
    }


    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return R.success("退出成功！");
    }

}
