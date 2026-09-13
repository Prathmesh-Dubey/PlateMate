package com.mess.app.repository;

import com.mess.app.entity.PartyExtraItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface PartyExtraItemRepository extends JpaRepository<PartyExtraItem, String> {

        List<PartyExtraItem> findByPartyId(String partyId);

        List<PartyExtraItem> findByPartyIdOrderByItemNameAsc(String partyId);

        @Modifying
        @Transactional
        @Query("DELETE FROM PartyExtraItem e WHERE e.party.id = :partyId")
        void deleteByPartyId(@Param("partyId") String partyId);

        @Modifying
        @Transactional
        @Query("DELETE FROM PartyExtraItem e WHERE e.party.id = :partyId AND e.id = :itemId")
        void deleteByPartyIdAndItemId(@Param("partyId") String partyId, @Param("itemId") String itemId);

        @Query("SELECT e.itemName, SUM(e.totalAmount) FROM PartyExtraItem e " +
                        "WHERE e.party.eventDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.itemName ORDER BY SUM(e.totalAmount) DESC")
        List<Object[]> getTopExtraItemsByDateRange(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT SUM(e.totalAmount) FROM PartyExtraItem e WHERE e.party.id = :partyId")
        BigDecimal getTotalExtraAmountByParty(@Param("partyId") String partyId);

        @Query("SELECT COUNT(e) FROM PartyExtraItem e WHERE e.party.id = :partyId")
        long countExtraItemsByParty(@Param("partyId") String partyId);

        @Query("SELECT e.itemName, COUNT(e) FROM PartyExtraItem e " +
                        "WHERE e.party.eventDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY e.itemName ORDER BY COUNT(e) DESC")
        List<Object[]> getMostFrequentExtraItems(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);
}