/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.microfocus.application.automation.tools.commonResultUpload.xmlreader;

import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;

public class ValueTranslator {

    private XpathReader xpathReader;

    public ValueTranslator(XpathReader xpathReader) {
        this.xpathReader = xpathReader;
    }

    public String translate(String fieldValue, Node node) throws XPathExpressionException {
        String[] arr = fieldValue.split("\\|");
        StringBuilder sb = new StringBuilder();
        for (String i : arr) {
            sb.append(translateUnit(i, node));
        }
        return sb.toString();
    }

    private String translateUnit(String fieldValue, Node node) throws XPathExpressionException {
        if ("v:".equals(fieldValue.substring(0, 2))) {
            fieldValue = fieldValue.substring(2, fieldValue.length());
        } else if ("x:".equals(fieldValue.substring(0, 2))) {
            fieldValue = fieldValue.substring(2, fieldValue.length());
            fieldValue = xpathReader.getValueFromNode(fieldValue, node);
        }
        return fieldValue;
    }
}
