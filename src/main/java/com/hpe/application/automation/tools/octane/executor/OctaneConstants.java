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

package com.hpe.application.automation.tools.octane.executor;

/**
 * Constants of Octane Entities
 */
public class OctaneConstants {

    public static class General{

        public static final String WINDOWS_PATH_SPLITTER = "\\";
        public static final String LINUX_PATH_SPLITTER = "/";
    }

    public static class Base {
        public static final String ID_FIELD = "id";
        public static final String NAME_FIELD = "name";
        public static final String LOGICAL_NAME_FIELD = "logical_name";
        public static final String DESCRIPTION_FIELD = "description";
    }

    public static class Tests extends Base {
        public static final String COLLECTION_NAME = "automated_tests";
        public static final String SCM_REPOSITORY_FIELD = "scm_repository";
        public static final String TESTING_TOOL_TYPE_FIELD = "testing_tool_type";
        public static final String TEST_TYPE_FIELD = "test_type";
        public static final String PACKAGE_FIELD = "package";
        public static final String EXECUTABLE_FIELD = "executable";
    }

    public static class DataTables extends Base {
        public static final String COLLECTION_NAME = "scm_resource_files";
        public static final String RELATIVE_PATH_FIELD = "relative_path";
        public static final String SCM_REPOSITORY_FIELD = "scm_repository";
    }

    public static class Executors extends Base {
        public static final String COLLECTION_NAME = "executors";
    }

}
