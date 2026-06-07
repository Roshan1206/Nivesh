package com.nivesh.library.exception;

public class CacheNotFoundException extends RuntimeException {
    public CacheNotFoundException(String cacheName) {
        super(cacheName + "not found.");
    }
}
