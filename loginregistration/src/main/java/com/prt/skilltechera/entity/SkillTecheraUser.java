package com.prt.skilltechera.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skilltechera_user",schema = "skilltecheraschema",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillTecheraUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Password is mandatory")
    private String password; // This should store the hashed password

    @Column(nullable = false, unique = true)
    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email is mandatory")
    private String email;

    @Column(nullable = false)
    @NotEmpty(message = "Email is mandatory")
    @JsonProperty("fullName")
    private String fullName;

    @Column(name = "temporary_email")
    private String temporaryEmail;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            schema = "skilltecheraschema",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
    }
}
