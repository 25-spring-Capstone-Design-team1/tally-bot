import java.util.List;

public class InfiniteIterator<E> {
    final List<E> list;
    int idx;

    private InfiniteIterator(List<E> list, int idx) {
        this.list = list;
        this.idx = idx;
    }

    public static <E> InfiniteIterator<E> begin(List<E> list) {
        return new InfiniteIterator<>(list, 0);
    }

    public InfiniteIterator<E> increment() {
        if(this.idx + 1 == list.size())
            return new InfiniteIterator<>(list, 0);
        else
            return new InfiniteIterator<>(list, idx + 1);
    }

    public E value() {
        return this.list.get(this.idx);
    }

    public final List<E> getInnerList() {
        return list;
    }
}
