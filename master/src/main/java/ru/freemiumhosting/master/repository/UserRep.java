package ru.freemiumhosting.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.freemiumhosting.master.model.User;

public interface UserRep extends JpaRepository<User,Long> {
}
