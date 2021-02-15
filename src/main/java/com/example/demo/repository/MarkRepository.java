package com.example.demo.repository;

import com.example.demo.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface MarkRepository extends JpaRepository<Mark, Integer> {
    Mark findFirstByName(String name);

    @Query("SELECT m.name, SUM (mq.quantity) AS MarkQuantity FROM Mark m LEFT JOIN MarkQuantity mq")
    List<Object> findAllMarksAndQuantity();

    @Query("SELECT m.name, SUM (mq.quantity) AS MarkQuantity FROM Mark m INNER JOIN MarkQuantity mq")
    List<Object> findAllMarksWithoutNullQuantity();

    @Query("SELECT m.name, mq.quantity FROM Mark m LEFT JOIN MarkQuantity mq ORDER BY mq.quantity")
    List<Object> findAllMarksWithArrayQuantity();

}
