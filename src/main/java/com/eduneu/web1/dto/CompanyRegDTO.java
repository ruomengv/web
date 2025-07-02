package com.eduneu.web1.dto;

public class CompanyRegDTO {
    private String companyName;
    private String contact;
    private String username;
    private String password;
    private String license;
    private String captcha; // 验证码

    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    public String getCaptcha() { return captcha; }
    public void setCaptcha(String captcha) { this.captcha = captcha; }
}