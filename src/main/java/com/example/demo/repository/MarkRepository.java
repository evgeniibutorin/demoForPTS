package com.example.demo.repository;

import com.example.demo.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MarkRepository extends JpaRepository<Mark, Integer> {
    Mark findFirstByName(String name);


}
