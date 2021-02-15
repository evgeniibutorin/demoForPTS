package com.example.demo.repository;

import com.example.demo.entity.Mark;
import com.example.demo.entity.MarkQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarkQuantityRepository extends JpaRepository<MarkQuantity, Integer> {


}
