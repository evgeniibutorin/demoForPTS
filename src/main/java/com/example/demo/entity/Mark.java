package com.example.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Mark {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;
    private String name;

    /*@OneToMany(mappedBy = "mark",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MarkQuantity> markQuantities;*/

}
