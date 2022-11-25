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
    @Column(name = "name")
    private String name;
    @Column(name = "link")
    private String link;
    @Column(name = "branch")
    private String branch;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.UNDEFINED;
    @Column(name = "language")
    private String language;
    @Column(name = "executableFileName")
    private String executableFileName;
    @Column(name = "lastLaunch")
    private String lastLaunch = "true";
    @Column(name = "currentLaunch")
    private String currentLaunch = "true";

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Project(String name, String link, ProjectStatus status, String language) {
        this.name = name;
        this.link = link;
        this.status = status;
        this.language = language;
    }

    public Project(String link) {
        this.link = link;
    }

    public Boolean userStartsDeploy() {
        return this.status == ProjectStatus.CREATED;
    }

    public Boolean userFinishesDeploy() {
        return this.status == ProjectStatus.RUNNING;
    }
}
