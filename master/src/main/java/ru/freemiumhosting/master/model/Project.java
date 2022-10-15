package ru.freemiumhosting.master.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name ="Project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projects_generator")
    @SequenceGenerator(name="projects_generator", sequenceName = "projects_seq", allocationSize=500)
    @Column(unique = true)
    private Long id;
    private String name;
    private String shortDescription;
    private String fullDescription;
    private String link;
    private String status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Project(String name, String shortDescription, String fullDescription, String link, String status) {
        this.name = name;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        this.link = link;
        this.status = status;
    }

    public Project(String link) {
        this.link = link;
    }
}
