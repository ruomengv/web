package com.eduneu.web1.dto;

public class PasswordDTO {
    private Long uid;
    private String oldPassword;
    private String newPassword;

    // Getters and Setters
    public Long getUid() { return uid; }
    public void setUid(Long uid) { this.uid = uid; }
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}