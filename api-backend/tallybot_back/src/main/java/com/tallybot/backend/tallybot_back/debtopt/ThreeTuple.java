package com.tallybot.backend.tallybot_back.debtopt;

public class ThreeTuple<E, F, G> {
    E f;
    F s;
    G t;

    public ThreeTuple(E first, F second, G third) {
        this.f = first;
        this.s = second;
        this.t = third;
    }

    public E first() {
        return f;
    }

    public F second() {
        return s;
    }

    public G third() {
        return t;
    }

    @Override
    public String toString() {
        return "(" + first() + ", " + second() + ", " + third() + ")";
    }
}