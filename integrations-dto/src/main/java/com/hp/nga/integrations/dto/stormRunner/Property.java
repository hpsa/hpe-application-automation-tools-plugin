package com.hp.nga.integrations.dto.stormRunner;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by lev on 31/05/2016.
 */
public interface Property  extends DTOBase {
    String getPropertyName();
    Property setPropertyName(String name);
    String getPropertyValue();
    Property setPropertyValue(String value);
}
