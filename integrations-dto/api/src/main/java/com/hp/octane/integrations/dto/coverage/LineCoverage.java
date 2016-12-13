package com.hp.octane.integrations.dto.coverage;

import com.hp.octane.integrations.dto.DTOBase;

/**
 * Created by gullery on 03/01/2016.
 */

public interface LineCoverage extends DTOBase {

	Integer getNumber();

	LineCoverage setNumber(int number);

	Integer getCount();

	LineCoverage setCount(int count);
}
