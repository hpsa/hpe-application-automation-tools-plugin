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

package com.hp.application.automation.tools.octane.tests;

import com.hp.application.automation.tools.octane.tests.junit.JUnitTestResult;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class TestResultIterable implements Iterable<JUnitTestResult> {

    private Reader reader;

    public TestResultIterable(File file) throws FileNotFoundException {
        this.reader = new FileReader(file);
    }

    public TestResultIterable(Reader reader) {
        this.reader = reader;
    }

    @Override
    public TestResultIterator iterator() {
        try {
            return new TestResultIterator(reader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
