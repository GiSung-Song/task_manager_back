package com.taskmanager.myapp.domain;

import com.taskmanager.myapp.config.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roles extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String roleName;

    public static Roles createRoles(String roleName) {
        Roles roles = new Roles();

        roles.setRoleName(roleName);

        return roles;
    }

    private void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
