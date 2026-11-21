package com.carmineabk.parcella.domain.enums;

public enum PackageSize {
    SMALL, MEDIUM, LARGE; // order matters: used by canCarry()

    public boolean canCarry(PackageSize required) {
        return this.compareTo(required) >= 0;
    }
}