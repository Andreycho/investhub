package com.example.investhub.repository;

import com.example.investhub.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByUser_Id(Long userId);
    Optional<Holding> findByUser_IdAndAsset_Symbol(Long userId, String assetSymbol);
}

