package com.eduneu.web1.entity;

public class Company {
    private Long id;
    private String name;
    private String contact;
    private String license;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
}