package ru.freemiumhosting.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.freemiumhosting.master.model.Env;
import ru.freemiumhosting.master.model.Project;
import ru.freemiumhosting.master.model.User;

import java.util.List;

@Repository
public interface EnvRep extends JpaRepository<Env,Long> {
    List<Env> findAllByProject(Project project);

    void deleteAllByProject(Project project);
}
