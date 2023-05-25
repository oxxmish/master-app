package ru.freemiumhosting.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.freemiumhosting.master.model.Project;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProjectRep extends JpaRepository<Project,Long> {
    long countByNameIgnoreCaseAndOwnerId(String name, Long ownerId);
    List<Project> findByOwnerName(@NonNull String name);
    Boolean existsByNodePort(Integer nodePort);

    Optional<Project> findByIdAndOwnerId(Long id, Long userId);
}
