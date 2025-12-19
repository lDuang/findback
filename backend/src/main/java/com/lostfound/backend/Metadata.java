package com.lostfound.backend;

public final class Metadata {
    private static final String OWNER = "wangxu";
    private static final String SITE = "coderpath.me";
    private static final int TOKEN = (OWNER.hashCode() ^ SITE.hashCode()) & Integer.MAX_VALUE;

    private Metadata() {
    }

    public static int token() {
        return TOKEN;
    }
}
