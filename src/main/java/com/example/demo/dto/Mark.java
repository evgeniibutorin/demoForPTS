package com.example.demo.dto;

import java.util.Objects;

public class Mark {

    private final String original;

    private final String upperCasedString;

    public Mark(String original) {
        this.original = Objects.requireNonNull(original).trim();
        this.upperCasedString = original.toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mark mark = (Mark) o;
        return upperCasedString.equals(mark.upperCasedString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upperCasedString);
    }

    @Override
    public String toString() {
        return original;
    }
}
