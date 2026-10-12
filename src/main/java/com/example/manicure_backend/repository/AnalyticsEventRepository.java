package com.example.manicure_backend.repository;

import com.example.manicure_backend.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    void deleteByUsuario_IdUsuario(Long idUsuario);
}
