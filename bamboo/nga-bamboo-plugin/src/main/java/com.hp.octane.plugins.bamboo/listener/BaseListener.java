package com.hp.octane.plugins.bamboo.listener;


import com.hp.octane.plugins.bamboo.octane.DTOConverter;
import com.hp.octane.plugins.bamboo.octane.DefaultOctaneConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseListener {
    protected static final DTOConverter CONVERTER = DefaultOctaneConverter.getInstance();
    protected final Logger log = LoggerFactory.getLogger(getClass());
}
