package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.CalculateDetail;
import com.tallybot.backend.tallybot_back.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculateDetailRepository extends JpaRepository<CalculateDetail, Long> {

    List<CalculateDetail> findByCalculate(Calculate calculate);
    List<CalculateDetail> findAllByCalculate(Calculate calculate);
    void deleteByCalculate(Calculate calculate);
}
