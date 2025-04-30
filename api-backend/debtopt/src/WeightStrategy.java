import java.util.List;
import java.util.Map;

public interface WeightStrategy {
    int getWeight(FlattedGraph f);
    String toString();
}
