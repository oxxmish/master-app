package ru.freemiumhosting.master.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logs")
public class Logs {
    @Id
    @Column(name = "project_id")
    Long projectId;

    @Column(name = "log_message")
    String logMessage;
}
