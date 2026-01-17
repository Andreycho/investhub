package com.example.investhub.repository;

import com.example.investhub.model.Transaction;
import com.example.investhub.model.enumeration.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type ORDER BY t.timestamp DESC")
    List<Transaction> findByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT SUM(t.quantity * t.pricePerUnit) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal calculateTotalAmountByUserAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query(value = "SELECT COUNT(*) FROM transactions t " +
                  "JOIN assets a ON t.asset_id = a.id " +
                  "WHERE t.user_id = :userId AND a.symbol = :symbol", 
           nativeQuery = true)
    Long countTransactionsByUserAndAsset(@Param("userId") Long userId, @Param("symbol") String symbol);
}