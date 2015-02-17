package com.hp.octane.plugins.jenkins.model.causes;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 20:18
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public class CIEventSCMCause implements CIEventCauseBase {
	@Override
	@Exported(inline = true)
	public CIEventCauseType getType() {
		return CIEventCauseType.SCM;
	}
}
