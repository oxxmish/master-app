package ru.freemiumhosting.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.freemiumhosting.master.model.User;

@Repository
public interface UserRep extends JpaRepository<User,Long> {
}
