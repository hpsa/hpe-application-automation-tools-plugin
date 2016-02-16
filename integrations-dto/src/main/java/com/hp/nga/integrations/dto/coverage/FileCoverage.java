package com.hp.nga.integrations.dto.coverage;

import com.hp.nga.integrations.dto.DTOBase;

/**
 * Created by gullery on 03/01/2016.
 */

public interface FileCoverage extends DTOBase {

	String getFile();

	FileCoverage setFile(String file);

	LineCoverage[] getLines();

	FileCoverage setLines(LineCoverage[] lines);
}
