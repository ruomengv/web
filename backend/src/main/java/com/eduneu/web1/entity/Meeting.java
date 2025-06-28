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
    private LocalDateTime startTime;  // Changed to LocalDateTime
    private LocalDateTime endTime;    // Changed to LocalDateTime
    private String content;
    private String status;

    public Meeting() {}

    public Meeting(String name, String organizer, LocalDateTime startTime, LocalDateTime endTime, String content, String status) {
        this.name = name;
        this.organizer = organizer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.content = content;
        this.status = status;
    }

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
                '}';
    }
}