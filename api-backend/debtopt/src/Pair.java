import java.util.Map;

public class Pair<E, F> {
    public E f;
    public F s;

    public Pair(E first, F second) {
        this.f = first;
        this.s = second;
    }

    public E first() {
        return f;
    }

    public F second() {
        return s;
    }

    @Override
    public String toString() {
        return "(" + f + ", " + s + ")";
    }

    public Pair(Map.Entry<E, F> m) {
        this.f = m.getKey();
        this.s = m.getValue();
    }
}
