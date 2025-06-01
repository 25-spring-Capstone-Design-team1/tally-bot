package com.tallybot.backend.tallybot_back.debtopt;

import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public final class FlattedGraph {
    Pair<InfiniteIterator<WeightStrategy>, ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>>> p;

    public FlattedGraph(InfiniteIterator<WeightStrategy> ws, ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>> t) {
        p = Pair.of(ws, t);
    }

    public FlattedGraph(InfiniteIterator<WeightStrategy> ws, List<Integer> circuit, Map<Integer, Integer> weightFrequency, List<Integer> weights) {
        this(
                ws, new ThreeTuple<>(circuit, weightFrequency, weights)
        );
    }

    public FlattedGraph(FlattedGraph f) {
        this(f.p.getFirst(), new ArrayList<>(f.p.getSecond().first()), new HashMap<>(f.p.getSecond().second()), new ArrayList<>(f.p.getSecond().third()));
    }

    public InfiniteIterator<WeightStrategy> getWeightStrategies() {
        return p.getFirst();
    }

    public void setWeightStrategies(InfiniteIterator<WeightStrategy> ws) {
        p = Pair.of(ws, p.getSecond());
    }

    public List<Integer> getCircuit() {
        return p.getSecond().first();
    }

    public void setChangeable(List<Integer> circuit) {
        p = Pair.of(p.getFirst(), new ThreeTuple<>(circuit, p.getSecond().second(), p.getSecond().third()));
    }

    public Map<Integer, Integer> getWeightFrequency() {
        return p.getSecond().second();
    }

    public void setWeightFrequency(Map<Integer, Integer> weightFrequency) {
        p = Pair.of(p.getFirst(), new ThreeTuple<>(p.getSecond().first(), weightFrequency, p.getSecond().third()));
    }

    public List<Integer> getWeights() {
        return p.getSecond().third();
    }

    public void setWeights(List<Integer> weights) {
        p = Pair.of(p.getFirst(), new ThreeTuple<>(p.getSecond().first(), p.getSecond().second(), weights));
    }

    public ThreeTuple<List<Integer>, Map<Integer, Integer>, List<Integer>> getSecond() {
        return p.getSecond();
    }
}
