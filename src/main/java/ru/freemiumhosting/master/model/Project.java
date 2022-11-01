package ru.freemiumhosting.master.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.AbstractMap;
import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projects_generator")
    @SequenceGenerator(name = "projects_generator", sequenceName = "projects_seq", allocationSize = 500)
    private Long id;
    private String name;
    private String link;
    private String branch;
    private String status = "Проект запущен успешно";//TODO change
    private String language;

    private Boolean launchedByUser = true;//TODO change

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Project(String name, String link, String status, String language) {
        this.name = name;
        this.link = link;
        this.status = status;
        this.language = language;
    }

    public Project(String link) {
        this.link = link;
    }
}
