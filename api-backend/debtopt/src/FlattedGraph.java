import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public final class FlattedGraph {
    Pair<InfiniteIterator<WeightStrategy>, ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>>> p;

    public FlattedGraph(InfiniteIterator<WeightStrategy> ws, ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>> t) {
        p = new Pair<>(ws, t);
    }

    public FlattedGraph(InfiniteIterator<WeightStrategy> ws, List<Integer> circuit, Map<Integer, Integer> weightFrequency, List<Integer> weights) {
        this(
                ws, new ThreeTuple<>(circuit, weightFrequency, weights)
        );
    }

    public FlattedGraph(FlattedGraph f) {
        this(f.p.first(), new ArrayList<>(f.p.second().first()), new HashMap<>(f.p.second().second()), new ArrayList<>(f.p.second().third()));
    }

    public InfiniteIterator<WeightStrategy> getWeightStrategies() {
        return p.first();
    }

    public void setWeightStrategies(InfiniteIterator<WeightStrategy> ws) {
        p = new Pair<>(ws, p.second());
    }

    public List<Integer> getCircuit() {
        return p.second().first();
    }

    public void setChangeable(List<Integer> circuit) {
        p = new Pair<>(p.first(), new ThreeTuple<>(circuit, p.second().second(), p.second().third()));
    }

    public Map<Integer, Integer> getWeightFrequency() {
        return p.second().second();
    }

    public void setWeightFrequency(Map<Integer, Integer> weightFrequency) {
        p = new Pair<>(p.first(), new ThreeTuple<>(p.second().first(), weightFrequency, p.second().third()));
    }

    public List<Integer> getWeights() {
        return p.second().third();
    }

    public void setWeights(List<Integer> weights) {
        p = new Pair<>(p.first(), new ThreeTuple<>(p.second().first(), p.second().second(), weights));
    }

    public ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>> getSecond() {
        return p.second();
    }
}
