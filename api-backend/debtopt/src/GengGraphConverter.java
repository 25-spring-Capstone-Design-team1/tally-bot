import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.Random;

public class GengGraphConverter {

    static Random rand = new Random();

    /**
     * 주어진 매개변수로 geng을 실행하고 생성된 그래프 목록을 반환합니다.
     *
     * @param vertices 정점 수
     * @param options geng 명령에 대한 추가 옵션 (예: "-c" 연결 그래프만 생성)
     * @return 생성된 Graph 객체 목록
     * @throws IOException 프로세스 실행 중 오류 발생
     */
    public static List<Graph> generateGraphs(int vertices, int edges, String options) throws IOException {
        List<Graph> graphs = new ArrayList<>();

        // geng 명령 구성
        String command = "geng " + options + " " + vertices + " " + edges;

        // geng 프로세스 실행
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        Process process = pb.start();

        // 프로세스 출력 읽기
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // nauty의 그래프 출력은 기본적으로 graph6 형식으로 출력됨
                // -g 옵션이 있으면 adjacency matrix 형식으로 출력됨
                if (options.contains("-g")) {
                    graphs.add(parseAdjacencyMatrix(line, vertices));
                } else {
                    graphs.add(parseGraph6Format(line));
                }
            }
        }

        // 프로세스가 완료될 때까지 대기
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("geng process exited with code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for geng process", e);
        }

        return graphs;
    }

    public static <T> T reduceGraph(int vertices, int edges, String options, T acc, BiFunction<T, Graph, T> func) throws IOException {
        // geng 명령 구성
        String command = "geng " + options + " " + vertices + " " + edges;

        // geng 프로세스 실행
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        Process process = pb.start();

        // 프로세스 출력 읽기
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // nauty의 그래프 출력은 기본적으로 graph6 형식으로 출력됨
                // -g 옵션이 있으면 adjacency matrix 형식으로 출력됨
                if (options.contains("-g")) {
                    acc = func.apply(acc, parseAdjacencyMatrix(line, vertices));
                } else {
                    acc = func.apply(acc, parseGraph6Format(line));
                }
            }
        }

        // 프로세스가 완료될 때까지 대기
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("geng process exited with code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for geng process", e);
        }

        return acc;
    }

    public static void consumeGraph(int vertices, int edges, String options, Consumer<Graph> consumer) throws IOException {
        reduceGraph(vertices, edges, options, null, (n, v) -> {consumer.accept(v); return null;});
    }

    public static <T> T randomReduceGraph(int vertices, int edges, int n, T acc, BiFunction<T, Graph, T> func) throws IOException {
        // geng 명령 구성
        String command = "genrang -g -e" + edges + " " + vertices + " " + n;

        // geng 프로세스 실행
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
        Process process = pb.start();

        // 프로세스 출력 읽기
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                acc = func.apply(acc, parseGraph6Format(line));
            }
        }

        // 프로세스가 완료될 때까지 대기
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("genrang process exited with code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for genrang process", e);
        }

        return acc;
    }

    /**
     * graph6 형식 문자열을 파싱하여 Graph 객체로 변환합니다.
     *
     * @param graph6String graph6 형식 문자열
     * @return 변환된 Graph 객체
     */
    private static Graph parseGraph6Format(String graph6String) {
        // graph6 형식 디코딩
        // 첫 바이트는 정점 수를 나타냄 (ASCII 63 + n)
        int n = graph6String.charAt(0) - 63;

        // 그래프 초기화
        Graph graph = new Graph(n);

        // 비트 위치 계산
        int bitPos = 0;
        // 상삼각 행렬에서의 각 비트가 간선의 존재 여부를 나타냄
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                // 바이트 위치와 비트 위치 계산
                int bytePos = 1 + (bitPos / 6);
                int bitInByte = 5 - (bitPos % 6);

                // 현재 바이트가 문자열 길이를 초과하지 않는지 확인
                if (bytePos < graph6String.length()) {
                    // 해당 비트가 1이면 간선 추가
                    int currentByte = graph6String.charAt(bytePos) - 63;
                    if ((currentByte & (1 << bitInByte)) != 0) {
                        // 가중치는 1로 설정 (geng에서는 가중치가 없음)
                        graph.addEdge(i, j, (rand.nextInt(n) + 1) * (rand.nextInt(2) * 2 - 1));
                    }
                }

                bitPos++;
            }
        }

        return graph;
    }

    /**
     * 인접 행렬 형식 문자열을 파싱하여 Graph 객체로 변환합니다.
     *
     * @param matrixString 인접 행렬을 나타내는 문자열
     * @param vertices 정점의 수
     * @return 변환된 Graph 객체
     */
    private static Graph parseAdjacencyMatrix(String matrixString, int vertices) {
        Graph graph = new Graph(vertices);

        // 공백 제거 및 행으로 분할
        String[] rows = matrixString.trim().split("\\s+");

        for (int i = 0; i < vertices; i++) {
            for (int j = 0; j < vertices; j++) {
                if (rows[i].charAt(j) == '1') {
                    // 각 간선은 한 번만 추가되도록 j > i 조건 추가
                    if (j > i) {
                        // 가중치는 1로 설정
                        graph.addEdge(i, j, (rand.nextInt(vertices) + 1) * (rand.nextInt(vertices) * 2 - 1));
                    }
                }
            }
        }

        return graph;
    }

    /**
     * 그래프를 분석하고 정보를 출력합니다.
     *
     * @param graph 분석할 그래프
     */
    public static void printGraphInfo(Graph graph) {
        System.out.println("그래프 정보:");
        System.out.println("정점 수: " + graph.getVertexCount());
        System.out.println("간선 수: " + graph.getEdgeCount());
        System.out.println("간선 목록:");

        for (int i = 0; i < graph.getVertexCount(); i++) {
            for (Map.Entry<Integer, Integer> edge : graph.getAdjacencyList().get(i).entrySet()) {
                // 각 간선은 한 번만 출력하기 위해 i < j 조건 추가
                if (i < edge.getKey()) {
                    System.out.println(i + " -- " + edge.getKey() + " [weight=" + edge.getValue() + "]");
                }
            }
        }
    }

    // 사용 예제
    public static void main(String[] args) {
        try {
            // 5개 정점을 가진 모든 연결 그래프 생성
            List<Graph> graphs = generateGraphs(5, 5, "-c");

            System.out.println("생성된 그래프 수: " + graphs.size());

            // 첫 번째 그래프 정보 출력
            if (!graphs.isEmpty()) {
                printGraphInfo(graphs.get(0));
            }
        } catch (IOException e) {
            System.err.println("그래프 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}