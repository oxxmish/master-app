package ru.freemiumhosting.master.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import ru.freemiumhosting.master.utils.converters.ListStringConverter;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static javax.persistence.CascadeType.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Project", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "owner_id"}))
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projects_generator")
    @SequenceGenerator(name = "projects_generator", sequenceName = "projects_seq", allocationSize = 1)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "type")
    private String type;
    @Column(name = "git_url")
    private String gitUrl;
    @Column(name = "git_branch")
    private String gitBranch;
    @Column(name = "app_link")
    private String appLink;
    @Column(name = "commit_hash")
    private String commitHash;
    @Column(name = "status")
    private ProjectStatus status = ProjectStatus.CREATED;
    @Column(name = "owner_id")
    private Long ownerId;
    @Column(name = "owner_name")
    private String ownerName;
    @Column(name = "created_date")
    private OffsetDateTime createdDate;
    @Column(name = "cpu_request")
    private Double cpuRequest;
    @Column(name = "cpu_consumption")
    private Double cpuConsumption;
    @Column(name = "ram_consumption")
    private Double ramConsumption;
    @Column(name = "ram_request")
    private Double ramRequest;
    @Column(name = "storage_consumption")
    private Double storageConsumption;
    @Column(name = "storage_request")
    private Double storageRequest;
    @Column(name = "registry_destination")
    private String registryDestination;
    @Column(name = "kubernetes_name")
    private String kubernetesName;
    @Column(name = "node_port")
    private Integer nodePort;

    @Convert(converter = ListStringConverter.class)
    private List<String> envs;

    @Convert(converter = ListStringConverter.class)
    private List<String> ports;

    public void generateAppLink(String domain){this.appLink= "http://" +domain+":"+this.nodePort;}
}
