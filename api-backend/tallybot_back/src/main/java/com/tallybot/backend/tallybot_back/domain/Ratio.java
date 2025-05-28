package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Embeddable
@Builder
@Getter
@EqualsAndHashCode
public class Ratio {
    @Column(name = "numerator", nullable = false, columnDefinition = "INT")
    private int numerator;

    @Column(name = "denominator", nullable = false, columnDefinition = "INT")
    private int denominator;

    public static int gcd(int a, int b) {
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

    public Ratio neg() {
        this.numerator = -this.numerator;
        return this;
    }

    public Ratio inv() {
        var tmp = inv(this);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
        return this;
    }

    public Ratio add(Ratio r) {
        var tmp = add(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
        return this;
    }

    public Ratio sub(Ratio r) {
        var tmp = sub(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
        return this;
    }

    public Ratio mul(Ratio r) {
        var tmp = mul(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
        return this;
    }

    public Ratio div(Ratio r) {
        var tmp = div(this, r);
        this.numerator = tmp.numerator;
        this.denominator = tmp.denominator;
        return this;
    }

    public double toDouble() {
        return (double)numerator / denominator;
    }

    public int toInt() {
        return numerator / denominator;
    }
}
