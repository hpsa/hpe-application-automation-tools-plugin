package com.emyoli.nga.listener;

import com.emyoli.nga.octane.DTOConverter;
import com.emyoli.nga.octane.DefaultOctaneConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseListener {
    protected static final DTOConverter CONVERTER = DefaultOctaneConverter.getInstance();
    protected final Logger log = LoggerFactory.getLogger(getClass());
}
