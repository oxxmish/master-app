package ru.freemiumhosting.master.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.CascadeType.*;

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
    private String appLink;
    private String branch;
    private String commitHash;
    private ProjectStatus status = ProjectStatus.UNDEFINED;//TODO change to enum
    private String language;
    private String executableName;
    private String registryDestination;
    private String lastLaunch = "true";
    private String currentLaunch = "true";
    private String kubernetesName;
    private Integer nodePort;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "project",cascade =ALL, orphanRemoval = false)
    private Set<Env> envs;

    public Boolean userStartsDeploy() {
        return lastLaunch.equals("false") && currentLaunch.equals("true");
    }

    public Boolean userFinishesDeploy() {
        return lastLaunch.equals("true") && currentLaunch.equals("false");
    }

    public void generateAppLink(String domain){this.appLink= "http://" +domain+":"+this.nodePort;}
}
