package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.UserGroup;
import com.tallybot.backend.tallybot_back.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByCalculate(Calculate calculate);
    List<Settlement> findByUserGroup(UserGroup userGroup);
    void deleteByCalculate(Calculate calculate);
    @Query("SELECT s FROM Settlement s " +
            "JOIN FETCH s.participants p " +
            "JOIN FETCH p.participantKey.member " +
            "WHERE s.calculate.calculateId = :calculateId")
    List<Settlement> findWithParticipantsByCalculateId(@Param("calculateId") Long calculateId);
    @Query("SELECT DISTINCT s FROM Settlement s JOIN FETCH s.participants WHERE s.calculate = :calculate")
    List<Settlement> findByCalculateWithParticipants(@Param("calculate") Calculate calculate);


}
