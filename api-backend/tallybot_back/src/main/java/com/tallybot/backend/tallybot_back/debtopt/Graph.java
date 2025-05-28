package com.tallybot.backend.tallybot_back.debtopt;

import org.springframework.data.util.Pair;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

public class Graph {
    // 인접 리스트: 각 정점(인덱스)은 (종점, 가중치) 쌍의 맵을 가짐
    private List<Map<Integer, Integer>> adjacencyList;
    // 그래프의 총 간선 수
    private int sz;

    // 생성자: 정점 수를 받아 그래프 초기화
    public Graph(int vertices) {
        adjacencyList = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new HashMap<>());
        }
        sz = 0;
    }

    public Graph(Graph g) {
        this(g.adjacencyList.size());
        for (int i = 0; i < g.adjacencyList.size(); i++) {
            this.adjacencyList.set(i, new HashMap<>(g.adjacencyList.get(i)));
        }
        sz = g.sz;
    }

    // 정점 수 반환
    public int getVertexCount() {
        return adjacencyList.size();
    }

    // 간선 수 반환
    public int getEdgeCount() {
        return sz;
    }

    public List<Map<Integer, Integer>> getAdjacencyList() {
        return adjacencyList;
    }

    // 간선 추가 메소드
    public void addEdge(int source, int destination, int weight) {
        // 입력 검증
        if (source < 0 || source >= adjacencyList.size() ||
                destination < 0 || destination >= adjacencyList.size()) {
            throw new IllegalArgumentException("Invalid vertex index");
        }

        // 이미 존재하는 간선인지 확인
        if (getWeight(source, destination) != Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Edge already exists");
        }

        // 양방향 간선 추가 (source -> destination: weight, destination -> source: -weight)
        adjacencyList.get(source).put(destination, weight);
        adjacencyList.get(destination).put(source, -weight);
        sz++;
    }

    // 간선 제거 메소드
    public void removeEdge(int source, int destination) {
        // 입력 검증
        if (source < 0 || source >= adjacencyList.size() ||
                destination < 0 || destination >= adjacencyList.size()) {
            throw new IllegalArgumentException("Invalid vertex index");
        }

        // source에서 destination으로 가는 간선 제거
        boolean removed = removeDirectedEdge(source, destination);

        // destination에서 source로 가는 간선 제거
        boolean removedReverse = removeDirectedEdge(destination, source);

        // 간선이 제거되었으면 간선 수 감소
        if (removed && removedReverse) {
            sz--;
        }
    }

    // 단방향 간선 제거 헬퍼 메소드
    private boolean removeDirectedEdge(int source, int dest) {
        Map<Integer, Integer> edges = adjacencyList.get(source);
        if (edges.containsKey(dest)) {
            edges.remove(dest);
            return true;
        }
        return false;
    }

    // 가중치 조회 메소드
    public int getWeight(int source, int destination) {
        // 입력 검증
        if (source < 0 || source >= adjacencyList.size() ||
                destination < 0 || destination >= adjacencyList.size()) {
            throw new IllegalArgumentException("Invalid vertex index");
        }

        // 간선이 없는 경우 최대값 반환
        return adjacencyList.get(source).getOrDefault(destination, Integer.MAX_VALUE);
    }

    // 가중치 덧셈 메소드
    public void plusWeight(int source, int destination, int diffWeight) {
        int weight = adjacencyList.get(source).getOrDefault(destination, 0) + diffWeight;
        removeEdge(source, destination);
        if(weight != 0)
          addEdge(source, destination, weight);
    }

    public void computeWeight(int source, int destination, UnaryOperator<Integer> remappingFunction) {
        // 입력 검증
        if (source < 0 || source >= adjacencyList.size() ||
                destination < 0 || destination >= adjacencyList.size()) {
            throw new IllegalArgumentException("Invalid vertex index");
        }

        boolean updated = false;

        // source -> destination 방향 가중치 업데이트

        if (adjacencyList.get(source).containsKey(destination)) {
            adjacencyList.get(source).compute(destination, (k, v) -> v == null ? remappingFunction.apply(0) : remappingFunction.apply(v));
            updated = true;
        }

        // destination -> source 방향 가중치 업데이트 (음수 가중치)
        if (adjacencyList.get(destination).containsKey(source)) {
            adjacencyList.get(destination).compute(source, (k, v) -> v == null ? -remappingFunction.apply(0) : -remappingFunction.apply(-v));
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("Edge does not exist");
        }
    }

    // BFS를 사용한 최단 경로 탐색 메소드
    public List<Integer> findShortestPath(int source, int destination) {
        // 입력 검증
        if (source < 0 || source >= adjacencyList.size() ||
                destination < 0 || destination >= adjacencyList.size()) {
            throw new IllegalArgumentException("Invalid vertex index");
        }

        // 이미 방문한 정점 추적
        boolean[] visited = new boolean[adjacencyList.size()];
        // 이전 정점 추적 (경로 재구성용)
        int[] parent = new int[adjacencyList.size()];
        Arrays.fill(parent, -1);

        // BFS를 위한 큐
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;

        // BFS 실행
        while (!queue.isEmpty()) {
            int current = queue.poll();

            // 목적지에 도달했으면 경로 재구성
            if (current == destination) {
                return reconstructPath(parent, source, destination);
            }

            // 현재 정점의 모든 인접 정점 탐색
            for (Integer dest : adjacencyList.get(current).keySet()) {
                if (!visited[dest]) {
                    visited[dest] = true;
                    parent[dest] = current;
                    queue.add(dest);
                }
            }
        }

        // 경로가 없는 경우 빈 리스트 반환
        return new ArrayList<>();
    }

    // 경로 재구성 헬퍼 메소드
    private List<Integer> reconstructPath(int[] parent, int source, int destination) {
        List<Integer> path = new ArrayList<>();

        // 목적지부터 시작하여 경로 거꾸로 구성
        for (int at = destination; at != -1; at = parent[at]) {
            path.add(at);
        }

        // 경로를 올바른 순서로 뒤집기
        Collections.reverse(path);

        // 경로가 source부터 시작하는지 확인
        if (path.get(0) != source) {
            return new ArrayList<>(); // 경로가 없는 경우
        }

        return path;
    }

    // 그래프 병합 메소드
    public static Graph merge(Graph g1, Graph g2) {
        // 더 큰 정점 수를 가진 그래프 찾기
        int maxVertices = Math.max(g1.getVertexCount(), g2.getVertexCount());

        // 새 그래프 생성
        Graph merged = new Graph(maxVertices);

        // g1의 모든 간선 추가
        for (int i = 0; i < g1.getVertexCount(); i++) {
            for (Integer dest : g1.adjacencyList.get(i).keySet()) {
                // 중복 추가 방지 (양방향 간선이므로 i < edge.destination일 때만 추가)
                if (i < dest) {
                    merged.plusWeight(i, dest, g1.adjacencyList.get(i).get(dest));
                }
            }
        }


        // g2의 모든 간선 추가
        for (int i = 0; i < g2.getVertexCount(); i++) {
            for (Integer dest : g2.adjacencyList.get(i).keySet()) {
                // 중복 추가 방지 (양방향 간선이므로 i < edge.destination일 때만 추가)
                if (i < dest) {
                    merged.plusWeight(i, dest, g2.adjacencyList.get(i).get(dest));
                }
            }
        }

        return merged;
    }


    // 그래프 출력 메소드 (디버깅용)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph with ").append(adjacencyList.size()).append(" vertices and ").append(sz).append(" edges:\n");

        for (int i = 0; i < adjacencyList.size(); i++) {
            sb.append(i).append(" -> ");
            for (Integer dest : adjacencyList.get(i).keySet()) {
                sb.append("(").append(dest).append(", ").append(adjacencyList.get(i).get(dest)).append(") ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public Pair<Integer, List<ThreeTuple<Integer, Integer, Integer>>> eulerize() {
        ArrayList<Integer> arr = new ArrayList<>();
        List<ThreeTuple<Integer, Integer, Integer>> res = new ArrayList<>();

        int startVertex = -1;

        for (int i = 0; i < adjacencyList.size(); i++) {
            if (adjacencyList.get(i).size() % 2 == 1) {
                arr.add(i);
            } else if(startVertex == -1 && !adjacencyList.get(i).isEmpty()) {
                startVertex = i;
            }
        }

        for (int i = 0; i < arr.size(); i += 2) {
            List<Integer> l = findShortestPath(arr.get(i), arr.get(i + 1));
            if (l.size() == 2) {
                int w = getWeight(arr.get(i), arr.get(i + 1));
                removeEdge(arr.get(i), arr.get(i + 1));
                l = findShortestPath(arr.get(i), arr.get(i + 1));
                if (l.isEmpty()) {
                    res.add(new ThreeTuple<>(arr.get(i), arr.get(i + 1), w));
                } else if(startVertex == -1) {
                    startVertex = arr.get(i);
                }
                for (int j = 0; j < l.size() - 1; j++) {
                    computeWeight(l.get(j), l.get(j + 1), v -> v + w);
                }
            } else {
                addEdge(arr.get(i), arr.get(i + 1), 0);
                if(startVertex == -1) {
                    startVertex = arr.get(i);
                }
            }
        }

        return Pair.of(startVertex, res);
    }

    // Graph 클래스에 추가할 메소드

    /**
     * Hierholzer 알고리즘을 사용하여 오일러 회로를 찾습니다.
     * 이미 그래프가 오일러 회로를 가진다고 가정합니다.
     *
     * @param startVertex 회로의 시작 정점
     * @return 오일러 회로 경로, 각 가중치별 빈도수를 담은 Map, 그리고 경로 상 간선 가중치 목록을 포함한 ThreeTuple
     */
// 수정된 findEulerCircuit 메소드
    public ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>> findEulerCircuit(int startVertex) {
        // 입력 검증
        if (startVertex < 0 || startVertex >= adjacencyList.size()) {
            throw new IllegalArgumentException("Invalid start vertex");
        }

        // 각 정점의 인접 맵을 복사 (원본 그래프를 변경하지 않기 위해)
        List<Map<Integer, Integer>> tempAdjList = new ArrayList<>();
        for (int i = 0; i < adjacencyList.size(); i++) {
            Map<Integer, Integer> edges = new HashMap<>(adjacencyList.get(i));
            tempAdjList.add(edges);
        }

        // 결과 경로와 가중치 맵
        Map<Integer, Integer> weightFrequency = new HashMap<>();
        List<Integer> circuit = new ArrayList<>();
        List<Integer> edgeWeights = new ArrayList<>();

        // 스택을 사용한 Hierholzer 알고리즘 구현
        Stack<Integer> stack = new Stack<>();
        Stack<Integer> path = new Stack<>();
        stack.push(startVertex);

        while (!stack.isEmpty()) {
            int v = stack.peek();

            if (!tempAdjList.get(v).isEmpty()) {
                // 인접 간선 중 하나 선택
                Map.Entry<Integer, Integer> q = tempAdjList.get(v).entrySet().iterator().next();
                Pair<Integer, Integer> entry = Pair.of(q.getKey(), q.getValue());
                int next = entry.getFirst();
                int weight = entry.getSecond();

                // 간선 제거 (양방향)
                tempAdjList.get(v).remove(next);
                tempAdjList.get(next).remove(v);

                // 가중치 빈도 업데이트
                weightFrequency.put(weight, weightFrequency.getOrDefault(weight, 0) + 1);

                // 다음 정점으로 이동
                stack.push(next);
                path.push(weight);
            } else {
                // 더 이상 방문할 간선이 없으면 경로에 추가
                circuit.add(0, v);
                stack.pop();

                if (!path.isEmpty()) {
                    edgeWeights.add(0, path.pop());
                }
            }
        }

        // edgeWeights의 크기를 circuit 크기 - 1로 맞추기
        if (edgeWeights.size() < circuit.size() - 1) {
            edgeWeights.add(0); // 마지막 간선을 0으로 처리
        }

        return new ThreeTuple<>(circuit, weightFrequency, edgeWeights);
    }


    public List<Integer> balances() {
        List<Integer> balances = new ArrayList<>();
        for (int i = 0; i < adjacencyList.size(); i++) {
            int sum = 0;

            for (Integer weight : adjacencyList.get(i).values()) {
                sum += weight;
            }

            sum = -sum;
            balances.add(sum);
        }

        return balances;
    }

    public static boolean equalBalances(List<Integer> a, List<Integer> b) {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }
        return true;
    }

    public void integrateSameWeight() {
        boolean flag = false;
        for (int i = 0; i < adjacencyList.size(); i++) {
            for (Integer d : adjacencyList.get(i).keySet()) {
                for (Integer d2 : adjacencyList.get(d).keySet()) {
                    if(i == d2) continue;
                    int w = adjacencyList.get(i).get(d);
                    if (w == adjacencyList.get(d).get(d2)) {
                        removeEdge(i, d);
                        removeEdge(d, d2);
                        addEdge(i, d2, w);
                        flag = true;
                        break;
                    }
                }
                if (flag) break;
            }
            if (flag) {
                i--;
                flag = false;
            }
        }
    }

    public static List<FlattedGraph> removeAndSplit(FlattedGraph f2, InfiniteIterator<WeightStrategy> weightStrategies) {
        if (f2.getCircuit().size() < 2) return List.of(f2);
        FlattedGraph f = new FlattedGraph(f2);
        boolean isCycle = f.getCircuit().get(0).equals(f.getCircuit().get(f.getCircuit().size() - 1));
        List<Integer> listToRemove = new ArrayList<>();

        List<FlattedGraph> res = new ArrayList<>();

        if (!isCycle) {
            f.getCircuit().add(f.getCircuit().get(0));
            f.getWeights().add(0);
            f.getWeightFrequency().put(0, f.getWeightFrequency().getOrDefault(0, 0) + 1);
        }

        int weightToRemove = weightStrategies.value().getWeight(f);
        InfiniteIterator<WeightStrategy> wss = weightStrategies.increment();

        for (int i = 0; i < f.getWeights().size(); i++) {
            if (f.getWeights().get(i) == weightToRemove) {
                listToRemove.add(i);
            }
        }

        for (int i = 0; i < listToRemove.size() - 1; i++) {
            List<Integer> newCircuit = new ArrayList<>(f.getCircuit().subList(listToRemove.get(i) + 1, listToRemove.get(i + 1) + 1));
            List<Integer> newWeights = new ArrayList<>(f.getWeights().subList(listToRemove.get(i) + 1, listToRemove.get(i + 1)));
            newWeights.replaceAll(w -> w - weightToRemove);
            Map<Integer, Integer> newWeightFrequency = new HashMap<>();
            for (Integer weight : newWeights) {
                newWeightFrequency.put(weight, newWeightFrequency.getOrDefault(weight, 0) + 1);
            }

            res.add(new FlattedGraph(wss, newCircuit, newWeightFrequency, newWeights));
        }

        List<Integer> newCircuit = new ArrayList<>(f.getCircuit().subList(listToRemove.get(listToRemove.size() - 1) + 1, f.getCircuit().size() - 1));
        newCircuit.addAll(f.getCircuit().subList(0, listToRemove.get(0) + 1));
        List<Integer> newWeights = new ArrayList<>(f.getWeights().subList(listToRemove.get(listToRemove.size() - 1) + 1, f.getWeights().size()));
        newWeights.addAll(f.getWeights().subList(0, listToRemove.get(0)));
        newWeights.replaceAll(w -> w - weightToRemove);
        Map<Integer, Integer> newWeightFrequency = new HashMap<>();
        for (Integer weight : newWeights) {
            newWeightFrequency.put(weight, newWeightFrequency.getOrDefault(weight, 0) + 1);
        }

        res.add(new FlattedGraph(wss, newCircuit, newWeightFrequency, newWeights));

        return res;
    }

    public static Graph summarize(Graph g) {
        List<WeightStrategy> wss = new ArrayList<>();
        wss.add(new MinMidRemove());
        wss.add(new MaxNumMidRemove());
        InfiniteIterator<WeightStrategy> weightStrategies = InfiniteIterator.begin(wss);
        List<Graph> l = UnionFind.splitGraph(g);
        Queue<Graph> q = new LinkedList<>(l);
        Graph resG = new Graph(g.getVertexCount());
        while(!q.isEmpty()) {
            Graph t = q.poll();

            if(t.getEdgeCount() < 2) {
                resG = Graph.merge(resG, t);
                continue;
            }

            t = t.defaultGraph();
            List<Graph> split = UnionFind.splitGraph(t);
            if(split.size() >= 2) {
                q.addAll(split);
                continue;
            }
            if(t.getEdgeCount() < 2) {
                resG = Graph.merge(resG, t);
                continue;
            }

            t.integrateSameWeight();
            split = UnionFind.splitGraph(t);
            if(split.size() >= 2) {
                q.addAll(split);
                continue;
            }
            if(t.getEdgeCount() < 2) {
                resG = Graph.merge(resG, t);
                continue;
            }

            Pair<Integer, List<ThreeTuple<Integer, Integer, Integer>>> p = t.eulerize();
            List<ThreeTuple<Integer, Integer, Integer>> removedEdge = p.getSecond();
            if(!p.getSecond().isEmpty()) {
                for(ThreeTuple<Integer, Integer, Integer> edge : removedEdge) {
                    resG.plusWeight(edge.first(), edge.second(), edge.third());
                }
            }

            split = UnionFind.splitGraph(t);
            if(split.size() >= 2) {
                q.addAll(split);
                continue;
            }
            if(t.getEdgeCount() < 2) {
                resG = Graph.merge(resG, t);
                continue;
            }

            ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>> res = t.findEulerCircuit(p.getFirst());

            Queue<FlattedGraph> qf = new LinkedList<>();
            List<FlattedGraph> afterCut = new ArrayList<>();
            qf.add(new FlattedGraph(weightStrategies, res));
            while(!qf.isEmpty()) {
                FlattedGraph fg = qf.poll();
                List<FlattedGraph> sfg = removeAndSplit(fg, fg.getWeightStrategies());
                if(sfg.size() == 1 && sfg.get(0).getCircuit().size() == fg.getCircuit().size()) {
                    afterCut.add(fg);
                } else {
                    qf.addAll(sfg);
                }
            }

            List<Graph> resList = new ArrayList<>();
            for(int i = 0; i < afterCut.size(); i++) {
                Graph fgGraph = new Graph(t.getVertexCount());
                fgGraph.addFlattedGraph(afterCut.get(i));
                resList.add(fgGraph);
            }

            if(resList.size() == 1)
                resG = Graph.merge(resG, resList.get(0));
            else
                q.addAll(resList);
        }

        resG.removeZero();
        return resG;
    }

    public Graph defaultGraph() {
        Graph res = new Graph(this.getVertexCount());
        List<Integer> b = balances();
        int d1 = -1;
        int c1 = -1;
        int posN = 1;
        int negN = 1;
        for (int i = 0; i < b.size(); i++) {
            if (b.get(i) > 0) {
                if (c1 == -1) {
                    c1 = i;
                } else {
                    posN++;
                }
            } else if (b.get(i) < 0) {
                if (d1 == -1) {
                    d1 = i;
                } else {
                    negN++;
                }
            }
        }

        if (posN + negN - 1 >= getEdgeCount()) return this;
        if (d1 == -1) return new Graph(this.getVertexCount());

        int cTotal = 0;
        for (int i = 0; i < b.size(); i++) {
            if (!(i == d1 || i == c1)) {
                if (b.get(i) > 0) {
                    res.addEdge(d1, i, b.get(i));
                    cTotal += b.get(i);
                } else if (b.get(i) < 0) {
                    res.addEdge(i, c1, -b.get(i));
                }
            }
        }

        if(-b.get(d1) - cTotal != 0)
            res.addEdge(d1, c1, -b.get(d1) - cTotal);
        return res;
    }

    private void removeZero() {
        for (int i = 0; i < this.getVertexCount(); i++) {
            adjacencyList.get(i).entrySet().removeIf(e -> e.getValue() == 0);
        }
    }

    private void addFlattedGraph(FlattedGraph f) {
        for (int i = 0; i < f.getCircuit().size() - 1; i++) {
            this.plusWeight(f.getCircuit().get(i), f.getCircuit().get(i + 1), f.getWeights().get(i));
        }
    }

    public static Graph fromString(String s) {
        Pattern p = Pattern.compile("-?[0-9]+");
        String[] lines = s.split("\n");
        String[] tokens = lines[0].split(" ");
        int vertexCount = Integer.parseInt(tokens[2]);
        Graph g = new Graph(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            String[] lineTokens = lines[i + 1].split("\\(");
            for(int j = 1; j < lineTokens.length; j++) {
                Matcher matcher = p.matcher(lineTokens[j]);

                while(matcher.find()) {
                    int end = Integer.parseInt(lineTokens[j].substring(matcher.start(), matcher.end()));
                    matcher.find();
                    int weight = Integer.parseInt(lineTokens[j].substring(matcher.start(), matcher.end()));
                    if(weight >= 0)
                        g.addEdge(i, end, weight);
                }
            }
        }
        return g;
    }
}

class MaxRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        int maxWeight = Integer.MIN_VALUE;
        for (int weight : f.getWeights()) {
            if (weight > maxWeight) {
                maxWeight = weight;
            }
        }

        return maxWeight;
    }

    @Override
    public String toString() {
        return "MaxRemove";
    }
}

class MinRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        int minWeight = Integer.MAX_VALUE;
        for (int weight : f.getWeights()) {
            if (weight < minWeight) {
                minWeight = weight;
            }
        }

        return minWeight;
    }

    @Override
    public String toString() {
        return "MinRemove";
    }
}

class MidPointRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        return f.getWeights().get(f.getWeights().size() / 2);
    }

    @Override
    public String toString() {
        return "MidPointRemove";
    }
}

class MaxMidRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        int[] idxs = new int[2];
        int maxIdx = (f.getWeights().size() - 1) / 2;
        int max = f.getWeights().get(maxIdx);

        if (f.getWeights().size() % 2 == 1) {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2 + 1;
        } else {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2;
        }

        for (; idxs[0] != -1; idxs[0]--, idxs[1]++) {
            if(max < f.getWeights().get(idxs[0])) {
                maxIdx = idxs[0];
                max = f.getWeights().get(idxs[0]);
            }
            if(max < f.getWeights().get(idxs[1])) {
                maxIdx = idxs[1];
                max = f.getWeights().get(idxs[1]);
            }
        }
        return max;
    }

    @Override
    public String toString() {
        return "MaxMidRemove";
    }
}

class MinMidRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        int[] idxs = new int[2];
        int minIdx = (f.getWeights().size() - 1) / 2;
        int min = f.getWeights().get(minIdx);

        if (f.getWeights().size() % 2 == 1) {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2 + 1;
        } else {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2;
        }

        for (; idxs[0] != -1; idxs[0]--, idxs[1]++) {
            if(min > f.getWeights().get(idxs[0])) {
                minIdx = idxs[0];
                min = f.getWeights().get(idxs[0]);
            }
            if(min > f.getWeights().get(idxs[1])) {
                minIdx = idxs[1];
                min = f.getWeights().get(idxs[1]);
            }
        }
        return min;
    }

    @Override
    public String toString() {
        return "MinMidRemove";
    }
}

class MaxNumRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        var m =  f.getSecond().second();
        int maxNum = Integer.MIN_VALUE;
        int maxNumIdx = -1;

        for(Integer k: m.keySet()) {
            if (m.get(k) > maxNum) {
                maxNum = m.get(k);
                maxNumIdx = k;
            }
        }

        return maxNumIdx;
    }

    @Override
    public String toString() {
        return "MaxNumRemove";
    }
}

class MinNumRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        var m =  f.getSecond().second();
        int minNum = Integer.MAX_VALUE;
        int minNumIdx = -1;

        for(Integer k: m.keySet()) {
            if (m.get(k) < minNum) {
                minNum = m.get(k);
                minNumIdx = k;
            }
        }

        return minNumIdx;
    }

    @Override
    public String toString() {
        return "MinNumRemove";
    }
}

class MaxNumMidRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        int[] idxs = new int[2];
        int maxNumIdx = (f.getWeights().size() - 1) / 2;
        int maxNum = f.getWeightFrequency().get(f.getWeights().get(maxNumIdx));

        if (f.getWeights().size() % 2 == 1) {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2 + 1;
        } else {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2;
        }

        for (; idxs[0] != -1; idxs[0]--, idxs[1]++) {
            if(maxNum < f.getWeightFrequency().get(f.getWeights().get(idxs[0]))) {
                maxNumIdx = idxs[0];
                maxNum = f.getWeightFrequency().get(f.getWeights().get(idxs[0]));
            }
            if(maxNum < f.getWeightFrequency().get(f.getWeights().get(idxs[1]))) {
                maxNumIdx = idxs[1];
                maxNum = f.getWeightFrequency().get(f.getWeights().get(idxs[1]));
            }
        }
        return f.getWeights().get(maxNumIdx);
    }

    @Override
    public String toString() {
        return "MaxNumMidRemove";
    }
}

class MinNumMidRemove implements WeightStrategy {
    @Override
    public int getWeight(FlattedGraph f) {
        int[] idxs = new int[2];
        int minNumIdx = (f.getWeights().size() - 1) / 2;
        int minNum = f.getWeightFrequency().get(f.getWeights().get(minNumIdx));

        if (f.getWeights().size() % 2 == 1) {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2 + 1;
        } else {
            idxs[0] = f.getWeights().size() / 2 - 1;
            idxs[1] = f.getWeights().size() / 2;
        }

        for (; idxs[0] != -1; idxs[0]--, idxs[1]++) {
            if(minNum > f.getWeightFrequency().get(f.getWeights().get(idxs[0]))) {
                minNumIdx = idxs[0];
                minNum = f.getWeightFrequency().get(f.getWeights().get(idxs[0]));
            }
            if(minNum > f.getWeightFrequency().get(f.getWeights().get(idxs[1]))) {
                minNumIdx = idxs[1];
                minNum = f.getWeightFrequency().get(f.getWeights().get(idxs[1]));
            }
        }
        return f.getWeights().get(minNumIdx);
    }

    @Override
    public String toString() {
        return "MinNumMidRemove";
    }
}