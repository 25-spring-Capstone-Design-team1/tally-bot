package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.UserGroup;
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

import java.util.Optional;

// 실제 테스트
@SpringBootTest
@ActiveProfiles({"test", "repository-test"})  // 다른 프로파일 조합으로 별도 컨텍스트 생성
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GroupRepositoryTest extends DatabaseTestBase {

    @Autowired
    private GroupRepository groupRepository;


    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("새로운 group 추가")
    void saveAndFindUserGroup() {
        // given
        UserGroup userGroup1 = new UserGroup(1753L,"새로운 톡방");
        groupRepository.save(userGroup1);
        UserGroup userGroup2 = new UserGroup(2782L, "2019 17기 Withme");
        groupRepository.save(userGroup2);
        UserGroup userGroup3 = new UserGroup(33038L, "삼겹살집 번개모임");
        groupRepository.save(userGroup3);

        // when
        long count = groupRepository.count();
        
        boolean existsTrue1 = groupRepository.existsById(1753L);
        boolean existsFalse1 = groupRepository.existsById(2000L);
        boolean existsTrue2 = groupRepository.existsById(2782L);
        boolean existsTrue3 = groupRepository.existsById(33038L);
        boolean existsFalse2 = groupRepository.existsById(8803L);
        boolean existsFalse3 = groupRepository.existsById(332038L);
        boolean existsTrue4 = groupRepository.existsById(2782L);
        boolean existsFalse4 = groupRepository.existsById(33838L);

        long count2 = groupRepository.count();

        Optional<UserGroup> findTrue1 = groupRepository.findById(1753L);
        Optional<UserGroup> findFalse1 = groupRepository.findById(2000L);
        Optional<UserGroup> findTrue2 = groupRepository.findById(2782L);
        Optional<UserGroup> findTrue3 = groupRepository.findById(33038L);
        Optional<UserGroup> findFalse2 = groupRepository.findById(8803L);
        Optional<UserGroup> findFalse3 = groupRepository.findById(332038L);
        Optional<UserGroup> findTrue4 = groupRepository.findById(2782L);
        Optional<UserGroup> findFalse4 = groupRepository.findById(33838L);

        // then
        assertThat(count).isEqualTo(3);
        assertThat(count2).isEqualTo(3);

        assertThat(existsTrue1).isTrue();
        assertThat(findTrue1).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getGroupId()).isEqualTo(1753L);
                    assertThat(user.getGroupName()).isEqualTo("새로운 톡방");
                });
        assertThat(existsTrue2).isTrue();
        assertThat(findTrue2).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getGroupId()).isEqualTo(2782L);
                    assertThat(user.getGroupName()).isEqualTo("2019 17기 Withme");
                });
        assertThat(existsTrue3).isTrue();
        assertThat(findTrue3).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getGroupId()).isEqualTo(33038L);
                    assertThat(user.getGroupName()).isEqualTo("삼겹살집 번개모임");
                });
        assertThat(existsTrue4).isTrue();
        assertThat(findTrue4).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getGroupId()).isEqualTo(2782L);
                    assertThat(user.getGroupName()).isEqualTo("2019 17기 Withme");
                });
        assertThat(existsFalse1).isFalse();
        assertThat(findFalse1).isEmpty().isNotPresent();
        assertThat(existsFalse2).isFalse();
        assertThat(findFalse2).isEmpty().isNotPresent();
        assertThat(existsFalse3).isFalse();
        assertThat(findFalse3).isEmpty().isNotPresent();
        assertThat(existsFalse4).isFalse();
        assertThat(findFalse4).isEmpty().isNotPresent();
    }
}