package dev.southside.utils;

@FunctionalInterface
public interface ISmoothInterpolator<T> {
   T getT(float f, T val);
}
