package com.eduneu.web1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String organizer;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String content;
    private String status;

    // --- 新增的“类别”字段 ---
    private String category;

    public Meeting() {}

    // --- 更新全参构造函数 ---
    public Meeting(String name, String organizer, LocalDateTime startTime, LocalDateTime endTime, String content, String status, String category) {
        this.name = name;
        this.organizer = organizer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.content = content;
        this.status = status;
        this.category = category; // 初始化新增字段
    }

    // --- 更新 toString() 方法 ---
    @Override
    public String toString() {
        return "Meeting{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", organizer='" + organizer + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}