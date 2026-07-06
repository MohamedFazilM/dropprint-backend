package com.dropprint.project.repository;

import com.dropprint.project.model.Design;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignRepository extends JpaRepository<Design, String> {
}