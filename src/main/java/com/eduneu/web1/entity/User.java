package com.eduneu.web1.entity;

import java.util.Date;

public class User {
    private Long uid;
    private String username;
    private String password;
    private String nickname;
    private String telephone;
    private String email;
    private Integer gender; // 0-未知 1-男 2-女
    private Integer status; // 0-禁用 1-启用
    private Integer role;   // 0-超级管理员 1-企业用户
    private Long companyId;
    private Date createTime;
    private Date updateTime;

    // 修改构造函数，正确初始化字段
    public User(long uid, String username, String password, int role) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User() {
        // 默认构造函数保持不变
    }

    public User(long l, String user1, String password, String mail, String number, int i, Date date, Date date1) {
    }

    // Getter 和 Setter 方法保持不变
    public Long getUid() { return uid; }
    public void setUid(Long uid) { this.uid = uid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public void setId(long l) {
    }

    public void setUserId(long l) {
    }


}