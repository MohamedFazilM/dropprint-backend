package com.dropprint.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdGeneratorService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String generate(String prefix, String sequenceName) {
        Long nextVal = jdbcTemplate.queryForObject(
                "select nextval('" + sequenceName + "')", Long.class
        );
        return prefix + "_" + String.format("%03d", nextVal);
    }
}