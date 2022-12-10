package ru.freemiumhosting.master.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name ="Env")
public class Env {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "env_generator")
    @SequenceGenerator(name="env_generator", sequenceName = "env_seq", allocationSize=500)
    @Column(unique = true)
    private Integer id;
    private String env_key;
    private String env_value;

    @ManyToOne()
    @JoinColumn(name = "project_id")
    private Project project;

    public Env(String key, String value,Project project){
        this.env_key =key;
        this.env_value=value;
        this.project=project;
    }
}
