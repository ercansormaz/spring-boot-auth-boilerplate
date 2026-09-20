package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

}
