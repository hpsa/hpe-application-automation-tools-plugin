package com.hp.octane.api;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Created by gullery on 29/12/2015.
 */

public interface JSONable {
	String toJSON() throws JsonProcessingException;
}
