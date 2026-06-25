package com.uber.bg.uber.bg.Entities;

import com.uber.bg.uber.bg.Entities.BaseEntity;
import com.uber.bg.uber.bg.Enumerations.USER_ROLE;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "createdOn")
    private LocalDateTime date = LocalDateTime.now();

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private USER_ROLE role;

}



