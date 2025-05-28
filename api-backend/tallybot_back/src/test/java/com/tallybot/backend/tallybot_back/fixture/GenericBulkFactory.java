package com.tallybot.backend.tallybot_back.fixture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GenericBulkFactory<T> {
    // 3개 파라미터용
    public static <T, P1, P2, P3> Builder3<T, P1, P2, P3> using(TriFunction<P1, P2, P3, T> constructor) {
        return new Builder3<>(constructor);
    }

    @FunctionalInterface
    public interface TriFunction<P1, P2, P3, T> {
        T apply(P1 p1, P2 p2, P3 p3);
    }

    // 3개 파라미터 빌더 - 가장 자주 사용
    public static class Builder3<T, P1, P2, P3> {
        private final TriFunction<P1, P2, P3, T> constructor;
        private final List<T> objects = new ArrayList<>();

        public Builder3(TriFunction<P1, P2, P3, T> constructor) {
            this.constructor = constructor;
        }

        // 핵심: add 메소드 - 파라미터들 받아서 객체 생성 후 List에 저장
        public Builder3<T, P1, P2, P3> add(P1 p1, P2 p2, P3 p3) {
            T newObject = constructor.apply(p1, p2, p3);  // 객체 생성
            objects.add(newObject);                        // List에 저장
            return this;
        }

        // 패턴으로 여러 개 추가
        public Builder3<T, P1, P2, P3> addPattern(int count,
                                                  Function<Integer, P1> p1Generator,
                                                  Function<Integer, P2> p2Generator,
                                                  Function<Integer, P3> p3Generator) {

            for (int i = 0; i < count; i++) {
                P1 p1 = p1Generator.apply(i);
                P2 p2 = p2Generator.apply(i);
                P3 p3 = p3Generator.apply(i);

                T newObject = constructor.apply(p1, p2, p3);  // 객체 생성
                objects.add(newObject);                        // List에 저장
            }
            return this;
        }

        // 조건부 추가
        public Builder3<T, P1, P2, P3> addIf(boolean condition, P1 p1, P2 p2, P3 p3) {
            if (condition) {
                add(p1, p2, p3);
            }
            return this;
        }

        // 최종 결과: 저장된 객체들의 List 반환
        public List<T> build() {
            return new ArrayList<>(objects);
        }

        // Repository와 연동하여 즉시 저장
        public <R extends JpaRepository<T, ?>> List<T> saveAllWith(R repository) {
            return repository.saveAll(build());
        }
    }

    // 4개 파라미터용
    public static <T, P1, P2, P3, P4> Builder4<T, P1, P2, P3, P4> using(QuadFunction<P1, P2, P3, P4, T> constructor) {
        return new Builder4<>(constructor);
    }

    @FunctionalInterface
    public interface QuadFunction<P1, P2, P3, P4, T> {
        T apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }

    // 3개 파라미터 빌더 - 가장 자주 사용
    public static class Builder4<T, P1, P2, P3, P4> {
        private final QuadFunction<P1, P2, P3, P4, T> constructor;
        private final List<T> objects = new ArrayList<>();

        public Builder4(QuadFunction<P1, P2, P3, P4, T> constructor) {
            this.constructor = constructor;
        }

        // 핵심: add 메소드 - 파라미터들 받아서 객체 생성 후 List에 저장
        public Builder4<T, P1, P2, P3, P4> add(P1 p1, P2 p2, P3 p3, P4 p4) {
            T newObject = constructor.apply(p1, p2, p3, p4);  // 객체 생성
            objects.add(newObject);                        // List에 저장
            return this;
        }

        // 패턴으로 여러 개 추가
        public Builder4<T, P1, P2, P3, P4> addPattern(int count,
                                                  Function<Integer, P1> p1Generator,
                                                  Function<Integer, P2> p2Generator,
                                                  Function<Integer, P3> p3Generator,
                                                  Function<Integer, P4> p4Generator) {

            for (int i = 0; i < count; i++) {
                P1 p1 = p1Generator.apply(i);
                P2 p2 = p2Generator.apply(i);
                P3 p3 = p3Generator.apply(i);
                P4 p4 = p4Generator.apply(i);

                T newObject = constructor.apply(p1, p2, p3, p4);  // 객체 생성
                objects.add(newObject);                        // List에 저장
            }
            return this;
        }

        // 조건부 추가
        public Builder4<T, P1, P2, P3, P4> addIf(boolean condition, P1 p1, P2 p2, P3 p3, P4 p4) {
            if (condition) {
                add(p1, p2, p3, p4);
            }
            return this;
        }

        // 최종 결과: 저장된 객체들의 List 반환
        public List<T> build() {
            return new ArrayList<>(objects);
        }

        // Repository와 연동하여 즉시 저장
        public <R extends JpaRepository<T, ?>> List<T> saveAllWith(R repository) {
            return repository.saveAll(build());
        }
    }
}