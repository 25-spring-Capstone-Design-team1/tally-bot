import java.util.*;

public class UnionFind {
    List<Integer> disjointSet;
    List<Integer> rank;

    public UnionFind(int vertices) {
        disjointSet = new ArrayList<>(vertices);
        rank = new ArrayList<>(Collections.nCopies(vertices, 1));
        for(int i = 0; i < vertices; i++) {
            disjointSet.add(i);
        }
    }

    public int find(int idx) {
        if(idx == disjointSet.get(idx)) return idx;

        disjointSet.set(idx, find(disjointSet.get(idx)));
        return find(disjointSet.get(idx));
    }

    public void union(int a, int b) {
        a = find(a);
        b = find(b);

        if(rank.get(a) < rank.get(b)) {
            int tmp = a;
            a = b;
            b = tmp;
        }

        disjointSet.set(b, a);
        rank.set(a, rank.get(a) + rank.get(b));
    }

    public static List<Graph> splitGraph(Graph g) {
        UnionFind uf = new UnionFind(g.getVertexCount());

        for(int i = 0; i < g.getVertexCount(); i++) {
            for(Integer end : g.getAdjacencyList().get(i).keySet()) {
                uf.union(i, end);
            }
        }

        Map<Integer, Graph> m = new HashMap<>();

        for(int i = 0; i < g.getVertexCount(); i++) {
            if(g.getAdjacencyList().get(i).isEmpty()) continue;
            final int i2 = i;
            int parent = uf.find(i);
            for(Integer j : g.getAdjacencyList().get(i).keySet()) {
                m.compute(parent, (k, v) -> {
                    if(v == null)
                        v = new Graph(g.getVertexCount());
                    if(i2 < j)
                        v.addEdge(i2, j, g.getAdjacencyList().get(i2).get(j));
                    return v;
                });
            }
        }

        return m.values().stream().toList();
    }
}
