package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.*;
import config.DatabaseTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 실제 테스트
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CalculateRepositoryTest extends DatabaseTestBase {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private CalculateRepository calculateRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("calculate 조회")
    void saveAndCaculateQuery() {
        // given
        UserGroup userGroup0 = new UserGroup(1753L,"새로운 톡방");
        groupRepository.save(userGroup0);
        UserGroup userGroup1 = new UserGroup(2782L, "2019 17기 Withme");
        groupRepository.save(userGroup1);
        UserGroup userGroup2 = new UserGroup(33038L, "삼겹살집 번개모임");
        groupRepository.save(userGroup2);

        LocalDateTime startTime = LocalDateTime.of(2024, 11, 8, 13, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 11, 9, 13, 0);

        Calculate cal0 = Calculate.builder()
                .calculateId(null)
                .startTime(startTime)
                .endTime(endTime)
                .status(CalculateStatus.CALCULATING)
                .userGroup(userGroup0)
                .build();
        calculateRepository.save(cal0);

        Calculate cal1 = Calculate.builder()
                .calculateId(null)
                .startTime(startTime)
                .endTime(endTime)
                .status(CalculateStatus.PENDING)
                .userGroup(userGroup1)
                .build();
        calculateRepository.save(cal1);

        Calculate cal2 = Calculate.builder()
                .calculateId(null)
                .startTime(startTime)
                .endTime(endTime)
                .status(CalculateStatus.COMPLETED)
                .userGroup(userGroup2)
                .build();
        calculateRepository.save(cal2);

        Calculate cal3 = Calculate.builder()
                .calculateId(null)
                .startTime(startTime)
                .endTime(endTime)
                .status(CalculateStatus.PENDING)
                .userGroup(userGroup0)
                .build();
        calculateRepository.save(cal3);

        Calculate cal4 = Calculate.builder()
                .calculateId(null)
                .startTime(startTime)
                .endTime(endTime)
                .status(CalculateStatus.CALCULATING)
                .userGroup(userGroup2)
                .build();
        calculateRepository.save(cal4);

        // when
        long cnt0 = calculateRepository.countByUserGroup(userGroup0);
        long cnt1 = calculateRepository.countByUserGroup(userGroup1);
        long cnt2 = calculateRepository.countByUserGroup(userGroup2);

        List<Calculate> find0 = calculateRepository.findByUserGroup(userGroup0);
        List<Calculate> find1 = calculateRepository.findByUserGroup(userGroup1);
        List<Calculate> find2 = calculateRepository.findByUserGroup(userGroup2);
        List<Calculate> findNo = calculateRepository.findByUserGroup(new UserGroup(userGroup0.getGroupId() + 1, userGroup0.getGroupName()));

        Optional<Calculate> id0 = calculateRepository.findById(cal0.getCalculateId());
        Optional<Calculate> id1 = calculateRepository.findById(cal1.getCalculateId());
        Optional<Calculate> id2 = calculateRepository.findById(cal2.getCalculateId());
        Optional<Calculate> id3 = calculateRepository.findById(cal3.getCalculateId());
        Optional<Calculate> id4 = calculateRepository.findById(cal4.getCalculateId());
        Optional<Calculate> idNo = calculateRepository.findById(cal4.getCalculateId() + 1);

        List<Calculate> all = calculateRepository.findAll();

        Optional<Calculate> cid0 = calculateRepository.findByCalculateId(cal0.getCalculateId());
        Optional<Calculate> cid1 = calculateRepository.findByCalculateId(cal1.getCalculateId());
        Optional<Calculate> cid2 = calculateRepository.findByCalculateId(cal2.getCalculateId());
        Optional<Calculate> cid3 = calculateRepository.findByCalculateId(cal3.getCalculateId());
        Optional<Calculate> cid4 = calculateRepository.findByCalculateId(cal4.getCalculateId());
        Optional<Calculate> cidNo = calculateRepository.findByCalculateId(cal4.getCalculateId() + 1);

        // then
        assertThat(cnt0).isEqualTo(2);
        assertThat(cnt1).isEqualTo(1);
        assertThat(cnt2).isEqualTo(2);

        assertThat(find0).containsAll(List.of(cal0, cal3));
        assertThat(find1).contains(cal1);
        assertThat(find2).containsAll(List.of(cal2, cal4));
        assertThat(findNo).isEmpty();

        assertThat(find0.size()).isEqualTo(2);
        assertThat(find1.size()).isEqualTo(1);
        assertThat(find2.size()).isEqualTo(2);

        assertThat(id0).isPresent();
        assertThat(id0).get().isEqualTo(cal0);
        assertThat(id1).isPresent();
        assertThat(id1).get().isEqualTo(cal1);
        assertThat(id2).isPresent();
        assertThat(id2).get().isEqualTo(cal2);
        assertThat(id3).isPresent();
        assertThat(id3).get().isEqualTo(cal3);
        assertThat(id4).isPresent();
        assertThat(id4).get().isEqualTo(cal4);
        assertThat(idNo).isEmpty();

        assertThat(all).containsAll(List.of(cal0, cal1, cal2, cal3, cal4));
        assertThat(all).size().isEqualTo(5);

        assertThat(cid0).isPresent();
        assertThat(cid0).get().isEqualTo(cal0);
        assertThat(cid1).isPresent();
        assertThat(cid1).get().isEqualTo(cal1);
        assertThat(cid2).isPresent();
        assertThat(cid2).get().isEqualTo(cal2);
        assertThat(cid3).isPresent();
        assertThat(cid3).get().isEqualTo(cal3);
        assertThat(cid4).isPresent();
        assertThat(cid4).get().isEqualTo(cal4);
        assertThat(cidNo).isEmpty();

    }
}