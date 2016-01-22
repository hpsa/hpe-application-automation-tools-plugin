package com.hpe.application.automation.tools.common;

public class Pair<TFirst, TSecond> {
    
    private final TFirst _first;
    private final TSecond _second;
    
    public Pair(TFirst first, TSecond second) {
        
        _first = first;
        _second = second;
    }
    
    public TFirst getFirst() {
        
        return _first;
    }
    
    public TSecond getSecond() {
        
        return _second;
    }
}
