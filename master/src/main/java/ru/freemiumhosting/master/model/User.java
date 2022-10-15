package ru.freemiumhosting.master.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.PERSIST;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name ="Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_generator")
    @SequenceGenerator(name="users_generator", sequenceName = "users_seq", allocationSize=500)
    @Column(unique = true)
    private Integer id;
    private String username;
    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private Set<Project> projects;

    public User(Integer id, String username){
        this.id =id;
        this.username=username;
    }
}
