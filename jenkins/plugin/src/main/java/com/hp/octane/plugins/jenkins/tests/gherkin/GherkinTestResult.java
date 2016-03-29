package com.hp.octane.plugins.jenkins.tests.gherkin;

import com.hp.octane.plugins.jenkins.tests.CustomTestResult;
import com.hp.octane.plugins.jenkins.tests.TestResultStatus;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by franksha on 20/03/2016.
 */
public class GherkinTestResult implements CustomTestResult{
  private Map<String, String> attributes;
  private Element contentElement;

  public GherkinTestResult(String name, Element xmlElement, long duration, TestResultStatus status) {
    this.attributes = new HashMap<String, String>();
    this.attributes.put("name", name);
    this.attributes.put("duration", String.valueOf(duration));
    this.attributes.put("status", status.toPrettyName());
    this.contentElement = xmlElement;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public Element getXmlElement() {
    return contentElement;
  }
}
