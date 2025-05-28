//package com.tallybot.backend.tallybot_back.repository;
//
//import com.tallybot.backend.tallybot_back.domain.Chat;
//import com.tallybot.backend.tallybot_back.domain.Group;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//class ChatRepositoryTest {
//
//    @Autowired
//    private ChatRepository chatRepository;
//
//    @Autowired
//    private GroupRepository groupRepository;
//
//    @Test
//    @DisplayName("시간 범위로 채팅 조회")
//    void findByGroupAndTimestampBetweenTest() {
//        // given
//        Group group = new Group();
//        group.setGroupName("테스트방");
//        groupRepository.save(group);
//
//        Chat chat1 = new Chat(null, group, LocalDateTime.of(2024, 5, 1, 10, 0), null, "안녕");
//        Chat chat2 = new Chat(null, group, LocalDateTime.of(2024, 5, 1, 10, 30), null, "반가워");
//        Chat chat3 = new Chat(null, group, LocalDateTime.of(2024, 5, 2, 12, 0), null, "잘 가");
//
//        chatRepository.saveAll(List.of(chat1, chat2, chat3));
//
//        // when
//        List<Chat> results = chatRepository.findByGroupAndTimestampBetween(
//                group,
//                LocalDateTime.of(2024, 5, 1, 9, 0),
//                LocalDateTime.of(2024, 5, 1, 11, 0)
//        );
//
//        // then
//        assertThat(results).hasSize(2);
//        assertThat(results).extracting(Chat::getMessage).containsExactlyInAnyOrder("안녕", "반가워");
//    }
//}
