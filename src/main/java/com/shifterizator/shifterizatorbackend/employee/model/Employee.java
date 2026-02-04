package com.shifterizator.shifterizatorbackend.employee.model;

import com.shifterizator.shifterizatorbackend.user.model.User;
import jakarta.persistence.*;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
