/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hpe.application.automation.tools.octane.model.processors.scm;

import com.hpe.application.automation.tools.settings.OctaneServerSettingsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by gullery on 31/03/2015.
 */

public enum SCMProcessors {
	NONE("hudson.scm.NullSCM", null),
	GIT("hudson.plugins.git.GitSCM", GitSCMProcessor.class),
	SVN("hudson.scm.SubversionSCM", SvnSCMProcessor.class);

	private static Logger logger = LogManager.getLogger(OctaneServerSettingsBuilder.class);
	private String targetSCMPluginClassName;
	private Class<? extends SCMProcessor> processorClass;

	SCMProcessors(String targetSCMPluginClassName, Class<? extends SCMProcessor> processorClass) {
		this.targetSCMPluginClassName = targetSCMPluginClassName;
		this.processorClass = processorClass;
	}

	public static SCMProcessor getAppropriate(String className) {
		SCMProcessor result = null;

		//  skip any processing if NULL SCM declared
		if (!className.startsWith(NONE.targetSCMPluginClassName)) {
			for (SCMProcessors p : values()) {
				if (className.startsWith(p.targetSCMPluginClassName))
					try {
						result = p.processorClass.newInstance();
						break;
					} catch (InstantiationException | IllegalAccessException e) {
						logger.error("failed to instantiate SCM processor of type '" + p.targetSCMPluginClassName, e);
					}
			}
			if (result == null) {
				result = getGenericSCMProcessor(className);
			}
		}

		return result;
	}

	private static SCMProcessor getGenericSCMProcessor(String className) {
		SCMProcessor genericSCMProcessor = null;
		try {
			genericSCMProcessor = (GenericSCMProcessor.class).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("failed to instantiate SCM processor of type '" + className, e);
		}
		return genericSCMProcessor;
	}
}
