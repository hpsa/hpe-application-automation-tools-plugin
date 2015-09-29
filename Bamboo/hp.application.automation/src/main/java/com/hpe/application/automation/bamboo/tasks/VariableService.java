/**
 © Copyright 2015 Hewlett Packard Enterprise Development LP

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.variable.VariableDefinition;
import com.atlassian.bamboo.variable.VariableDefinitionFactory;
import com.atlassian.bamboo.variable.VariableDefinitionFactoryImpl;
import com.atlassian.bamboo.variable.VariableDefinitionManager;

public class VariableService {

    private final VariableDefinitionManager variableDefinitionManager;

    public VariableService(VariableDefinitionManager variableDefinitionManager)
    {
        this.variableDefinitionManager = variableDefinitionManager;
    }

    public VariableDefinition getGlobalByKey(String key) {
        return variableDefinitionManager.getGlobalVariableByKey(key);
    }

    public VariableDefinition saveGlobalVariable(String key, String value) {
        // enforce uniqueness --
        // bamboo seems to not check this constraint, but you do actually need to do it
        VariableDefinition searchVar = variableDefinitionManager.getGlobalVariableByKey(key);

        if(searchVar == null) { // doesn't exist, add it
            VariableDefinitionFactory variableDefinitionFactory = new VariableDefinitionFactoryImpl();
            VariableDefinition newVar = variableDefinitionFactory.createGlobalVariable(key, value);
            variableDefinitionManager.saveVariableDefinition(newVar);
            return newVar;
        } else { // does exist, find the existing VariableDefinition and update it
            long id = searchVar.getId();
            VariableDefinition updateVar = variableDefinitionManager.findVariableDefinition(id);
            updateVar.setValue(value);
            variableDefinitionManager.saveVariableDefinition(updateVar);
            return updateVar;
        }
    }

    public void deleteGlobalVariable(String variableKey) {
        VariableDefinition searchVar = variableDefinitionManager.getGlobalVariableByKey(variableKey);
        if(searchVar != null) { // found it, let's nuke it
            variableDefinitionManager.deleteVariableDefinition(searchVar);
        }
        // else do nothing -- nothing to delete
    }
}
