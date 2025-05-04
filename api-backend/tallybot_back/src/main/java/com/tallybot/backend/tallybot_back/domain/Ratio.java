package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Ratio {
    private int numerator;
    private int denominator;

    private static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public Ratio(int numerator, int denominator) {
        if(denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero");
        }

        int gcd = gcd(Math.abs(denominator), Math.abs(numerator));
        this.numerator = (denominator < 0 ? -1 : 1) * numerator / gcd;
        this.denominator = Math.abs(denominator) / gcd;
    }

    public Ratio(int i) {
        this.numerator = i;
        this.denominator = 1;
    }

    public Ratio() {
        this.numerator = this.denominator = 1;
    }

    public static Ratio neg(Ratio r) {
        return new Ratio(-r.numerator, r.denominator);
    }

    public static Ratio inv(Ratio r) {
        return new Ratio(r.denominator, r.numerator);
    }

    public static Ratio add(Ratio r, Ratio s) {
        return new Ratio(r.numerator * s.denominator + r.denominator * s.numerator, r.denominator * s.denominator);
    }

    public static Ratio sub(Ratio r, Ratio s) {
        return add(r, neg(s));
    }

    public static Ratio mul(Ratio r, Ratio s) {
        return new Ratio(r.numerator * s.numerator, r.denominator * s.denominator);
    }

    public static Ratio div(Ratio r, Ratio s) {
        return mul(r, inv(s));
    }

    public void neg() {
        this.numerator = -this.numerator;
    }

    public void inv() {
        var tmp = inv(this);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
    }

    public void add(Ratio r) {
        var tmp = add(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
    }

    public void sub(Ratio r) {
        var tmp = sub(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
    }

    public void mul(Ratio r) {
        var tmp = mul(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
    }

    public void div(Ratio r) {
        var tmp = div(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
    }
}
