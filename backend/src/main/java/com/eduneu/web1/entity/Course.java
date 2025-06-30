package com.eduneu.web1.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Course {
    private Long id;
    private String title;
    private String cover;
    private String summary;
    private Integer sort;
    private String videoUrl;
    private String author;
    private Integer status;
    private String rejectReason;
    private Long creatorId;

    private Date create_time;
    private Date update_time;


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public Date getCreateTime() { return create_time; }
    public void setCreateTime(Date createTime) { this.create_time = createTime; }
    public Date getUpdateTime() { return update_time; }
    public void setUpdateTime(Date updateTime) { this.update_time = updateTime; }
}