package com.hp.octane.plugins.jenkins.model.api;

import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import hudson.model.ParameterValue;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gullery on 19/02/2015.
 */

@ExportedBean
public class ParameterInstance extends ParameterConfig {
	private Object value;

	public ParameterInstance(ParameterConfig pc) {
		super(pc);
	}

	public ParameterInstance(ParameterConfig pc, String value) {
		super(pc);
		this.value = value;
	}

	public ParameterInstance(ParameterConfig pc, ParameterValue value) {
		super(pc);
		this.value = value == null ? null : value.getValue();
	}

	@Exported(inline = true)
	public Object getValue() {
		return value;
	}


    /**
   * Normalize subProject name based on axis parameters combination
   * @param parameters
   * @return
   */
  public static String generateSubBuildName(ParameterInstance[] parameters){
    List<ParameterInstance> sortedList = new ArrayList<ParameterInstance>();
    for(ParameterInstance p : parameters) {
      if(p.getType() == ParameterType.AXIS.toString()) {
        sortedList.add(p);
      }
    }

    Collections.sort(sortedList, new Comparator<ParameterInstance>() {
      @Override
      public int compare(ParameterInstance p1, ParameterInstance p2) {
        return p1.getName().compareTo(p2.getName());
      }
    });

    String subBuildName = "";
    if(sortedList.size() > 0) {
      int i = 0;
      for (; i < sortedList.size() - 1; i++) {
        subBuildName += sortedList.get(i).getName() + "=" + sortedList.get(i).getValue().toString() + ",";
      }
      subBuildName += sortedList.get(i).getName() + "=" + sortedList.get(i).getValue().toString();
    }
    return subBuildName;
  }
}
