package com.example.ordersaga;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SagaEventLogRepository extends JpaRepository<SagaEventLog, Long> {
    List<SagaEventLog> findAllByOrderByCreatedAtDesc();
}
