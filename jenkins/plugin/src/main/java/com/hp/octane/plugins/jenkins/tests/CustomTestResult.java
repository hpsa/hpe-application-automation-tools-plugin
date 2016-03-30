package com.hp.octane.plugins.jenkins.tests;

import org.w3c.dom.Element;

import java.util.Map;

/**
 * Created by franksha on 20/03/2016.
 */
public interface CustomTestResult {
  /**
   * Gets a map of test run attributes: attribute name to value
   * @return a map of test run attributes: attribute name to value
   */
  Map<String, String> getAttributes();

  /**
   * Gets custom xml content element
   * @return custom xml content element
   */
  Element getXmlElement();
}
