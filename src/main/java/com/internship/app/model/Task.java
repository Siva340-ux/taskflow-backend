package com.internship.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "completed")
    private Boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}