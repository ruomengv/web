package com.eduneu.web1.mapper;
import  com.eduneu.web1.entity.Company;

import org.apache.ibatis.annotations.*;

@Mapper
public interface CompanyMapper {
    @Insert("INSERT INTO company(name, contact, license) VALUES(#{name}, #{contact}, #{license})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCompany(Company company);
}