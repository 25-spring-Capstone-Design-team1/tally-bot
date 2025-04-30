import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void mainPrint() {
        List<WeightStrategy> ws = new ArrayList<WeightStrategy>();
        ws.add(new MinMidRemove());
        ws.add(new MaxNumMidRemove());
        InfiniteIterator<WeightStrategy> iiws = InfiniteIterator.begin(ws);
        var start2 = System.nanoTime();
        try {
            for (int i = 3; i <= 30; i++) {
                for (int j = 3; j <= (i - 1) * i / 2; j++) {
                    ThreeTuple<Integer, Double, ThreeTuple<Integer, Integer, Long>> res = GengGraphConverter.randomReduceGraph(i, j, 1000,
                    new ThreeTuple<>(0, 0.0, new ThreeTuple<>(0, 0, 0L)), (acc, g) -> {
                        var start = System.nanoTime();
                        Graph g2 = Graph.summarize(g, iiws);
                        var end = System.nanoTime();

                        return new ThreeTuple<>(acc.first() + 1,
                                acc.second() + (double) (g.getEdgeCount() - g2.getEdgeCount()) / g.getEdgeCount(),
                                new ThreeTuple<>(acc.third().first() + (Graph.equalBalances(g.balances(), g2.balances()) ? 0 : 1),
                                        acc.third().second() + (g.getEdgeCount() < g2.getEdgeCount() ? 1 : 0),
                                        acc.third().third() + (end - start)));
                    });

                    /*System.out.printf("%d\t%d\t%d\t%f%%\t%f%%\t%f%%\t%d.%03d\n",
                            i, j, res.first(),
                            (res.second() / res.first() * 100),
                            ((double) res.third().first() / res.first() * 100),
                            ((double) res.third().second() / res.first() * 100),
                            res.third().third() / 1000L, res.third().third() % 1000L);*/
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        var end2 = System.nanoTime();
        var diff = end2 - start2;
        System.out.printf("%d.%09d seconds\n", diff / 1000000000L, diff % 1000000000L);
    }

    public static void mainDebug() {

    }

    public static void main(String[] args) {
        mainPrint();
    }
}

