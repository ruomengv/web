package com.eduneu.web1.controller;

import com.eduneu.web1.dto.CompanyRegDTO;
import com.eduneu.web1.entity.Company;
import com.eduneu.web1.entity.User;
import com.eduneu.web1.mapper.CompanyMapper;
import com.eduneu.web1.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CompanyMapper companyMapper;

    // 企业注册
    @PostMapping("/register")
    public String register(@RequestBody CompanyRegDTO dto, HttpSession session) {
        // 验证码校验
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(dto.getCaptcha())) {
            return "验证码错误";
        }

        // 插入企业信息
        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setContact(dto.getContact());
        company.setLicense(dto.getLicense());
        companyMapper.insertCompany(company);

        // 创建企业管理员用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(1); // 企业用户
        user.setCompanyId(company.getId());
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        return "注册成功，等待审核";
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        User loggedInUser = userMapper.login(user);
        if (loggedInUser != null) {
            // 登录成功，将用户信息存入 session
            session.setAttribute("currentUser", loggedInUser);

            // 返回更多用户信息给前端
            Map<String, Object> response = new HashMap<>();
            response.put("user", loggedInUser);
            response.put("sessionId", session.getId());

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登录失败");
    }

    // 用户登出
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "登出成功";
    }

    // 获取当前登录用户
    @GetMapping("/current-user")
    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    // 生成验证码
    @GetMapping("/captcha")
    public void generateCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        // 生成随机验证码
        String captcha = RandomStringUtils.randomAlphanumeric(4);
        session.setAttribute("captcha", captcha);

        // 生成图片验证码
        BufferedImage image = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 30);
        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(captcha, 20, 20);
        g.dispose();

        // 输出图片
        response.setContentType("image/jpeg");
        javax.imageio.ImageIO.write(image, "jpeg", response.getOutputStream());
    }
}