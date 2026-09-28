package com.leclowndu93150.thaumaturge.client.taint.overlay;

public record UvRect(int x, int y, int width, int height) {
    public int area() {
        return width * height;
    }
}
