package ru.freemiumhosting.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.freemiumhosting.master.model.Project;

public interface ProjectRep extends JpaRepository<Project,Long> {
}
