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

    @Column(nullable = false)
    private int level;

    public static Roles createRoles(String roleName, int level) {
        Roles roles = new Roles();

        roles.setRoleName(roleName);
        roles.setLevel(level);

        return roles;
    }

    private void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    private void setLevel(int level) {
        this.level = level;
    }
}
