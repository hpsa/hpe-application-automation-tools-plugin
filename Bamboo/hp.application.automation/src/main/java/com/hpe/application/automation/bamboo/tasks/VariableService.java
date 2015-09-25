package com.hpe.application.automation.bamboo.tasks;

import com.atlassian.bamboo.variable.VariableDefinition;
import com.atlassian.bamboo.variable.VariableDefinitionFactory;
import com.atlassian.bamboo.variable.VariableDefinitionFactoryImpl;
import com.atlassian.bamboo.variable.VariableDefinitionManager;

/**
 * Created by mprilepina on 20/08/2015.
 */
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
            if(updateVar != null) {
                updateVar.setValue(value);
            }
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
