package ru.freemiumhosting.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.freemiumhosting.master.model.Logs;

public interface LogsRepository extends JpaRepository<Logs, Long> {
}