function octane_job_configuration(target, progress, proxy) {

    if (typeof jQuery === 'undefined') {
        return {
            configure: function() {
                target.innerHTML = "JQuery plugin must be installed and enabled in <a href=" + rootURL + "/pluginManager>Plugin Manager</a>";
            }
        }
    }

    var originalUnload = window.onbeforeunload;

    var $ = jQuery;

    function caseInsensitiveStringEquals(left, right) {
        // TODO: janotav: no easy way to do this in JS (?), need to implement this properly
        return left.toLowerCase() === right.toLowerCase();
    }

    function configure() {
        progressFunc("Retrieving configuration from server");
        proxy.loadJobConfigurationFromServer(function (t) {
            progressFunc();
            var response = t.responseObject();
            if (response.errors) {
                response.errors.forEach(function (error) {
                    var errorDiv = $("<div class='error'><font color='red'><b/></font></div>");
                    errorDiv.find("b").text(error);
                    $(target).append(errorDiv);
                });
            } else {
                renderConfiguration(response);
            }
        });
    }

    function progressFunc(msg) {
        if (typeof msg === 'string') {
            $(progress).find("h3").text(msg);
            $(progress).show();
            $(target).hide();
        } else {
            $(progress).hide();
            $(target).show();
        }
    }

    function renderConfiguration(jobConfiguration, pipelineId) {
        var result = $(target);
        result.empty();

        var pipelineDiv = $("<div>");
        result.append(pipelineDiv);

        var buttons = $("<div>");

        var status = $("<div>");
        buttons.append(status);

        var tagTypes = {};
        var allTags = {};
        var tagTypesByName = {};
        jobConfiguration.taxonomies.forEach(function (tagType) {
            tagTypes[tagType.tagTypeId] = tagType;
            tagTypesByName[tagType.tagTypeName] = tagType;
            tagType.values.forEach(function (tag) {
                allTags[tag.tagId] = {
                    tagId: tag.tagId,
                    tagName: tag.tagName,
                    tagTypeId: tagType.tagTypeId,
                    tagTypeName: tagType.tagTypeName
                };
            });
        });

        var fieldTypes = {};
        jobConfiguration.fields.forEach(function (fieldType) {
            fieldTypes[fieldType.logicalListName] = fieldType;
        });

        var validators = [];
        var apply = [];
        var dirtyFlag;

        function initialize() {
            validators.length = 0;
            apply.length = 0;
            clearDirty();

            pipelineDiv.empty();
        }

        function addApplyButton(caption, pipeline, func, callback) {
            var applyButton = $("<input type='button'>");
            applyButton.prop('value', caption);
            applyButton.unbind('click').click(function() {
                saveConfiguration(pipeline, func, callback);
            });
            buttons.append(applyButton);
            pipelineDiv.append(buttons);
        }

        function renderPipelineMetadata(pipeline) {
            pipelineDiv.append("Pipeline: ");
            if (pipeline.isRoot) {
                var input = $("<input type='text' placeholder='Pipeline name'>");
                input.attr("value", pipeline.name);
                apply.push(function() {
                    pipeline.name = input.val();
                });
                enableDirtyInputCheck(input);
                addInputWithValidation(input, pipelineDiv, "Pipeline name must be specified");
            } else {
                pipelineDiv.append(pipeline.name);
            }

            pipelineDiv.append($("<br>"));

            if (pipeline.isRoot) {
                pipelineDiv.append("Release: ");
                var select = $("<select>");
                for (var releaseId in jobConfiguration.releases) {
                    select.append(new Option(jobConfiguration.releases[releaseId], releaseId, (pipeline.releaseId === releaseId)));
                }
                apply.push(function () {
                    pipeline.releaseId = select.val();
                });
                enableDirtySelectCheck(select);
                pipelineDiv.append(select).append($("<br>"));
            }
        }

        function renderNewPipeline(pipeline) {

            function createPipelineFunc(pipeline, callback) {
                proxy.createPipelineOnServer(pipeline, callback);
            }

            function createPipelineCallback(pipeline, response) {
                pipeline.id = response.id;

                // fill pipeline fieldTags based on metadata received in initial load
                pipeline.fieldTags = [];
                jobConfiguration.fields.forEach(function (fieldType) {
                    pipeline.fieldTags.push({
                        logicalListName: fieldType.logicalListName,
                        listId: fieldType.listId,
                        listName: fieldType.listName,
                        extensible: fieldType.extensible,
                        multiValue: fieldType.multiValue,
                        values: [] // assumption: nothing is pre-selected
                    });
                });
                renderConfiguration(jobConfiguration, pipeline.id);
            }

            initialize();
            renderPipelineMetadata(pipeline);
            addApplyButton('Create', pipeline, createPipelineFunc, createPipelineCallback);
        }

        function renderExistingPipeline(pipeline) {

            function saveFunc(pipeline, callback) {
                proxy.updatePipelineOnSever(pipeline, callback);
            }

            function saveCallback(pipeline, response) {
                pipeline.taxonomyTags = response.taxonomyTags;

                // merge newly created taxonomies with the existing ones in order to appear in drop-downs
                pipeline.taxonomyTags.forEach(function (taxonomy) {
                    var type = tagTypes[taxonomy.tagTypeId];
                    if (!type) {
                        type = {
                            tagTypeId: taxonomy.tagTypeId,
                            tagTypeName: taxonomy.tagTypeName,
                            values: []
                        };
                        jobConfiguration.taxonomies.push(type);
                        tagTypes[type.tagTypeId] = type;
                    }
                    var matchTag = function (tag) {
                        return tag.tagId == taxonomy.tagId;
                    };
                    if (!type.values.some(matchTag)) {
                        type.values.push({
                            tagId: taxonomy.tagId,
                            tagName: taxonomy.tagName
                        });
                    }
                });

                // merge newly created field values with existing ones in order to appear in drop-downs
                response.fields.forEach(function (receivedField) {
                    var fieldType = fieldTypes[receivedField.parentLogicalName];
                    var matchFiledValue = function(value) {
                        return value.id == receivedField.id;
                    };
                    if (!fieldType.values.some(matchFiledValue)) {
                        fieldType.values.push({
                            id: receivedField.id,
                            name: receivedField.name
                        });
                        pipeline.fieldTags.forEach(function (fieldTag) {
                            if (fieldTag.logicalListName === fieldType.logicalListName) {
                                fieldTag.values.forEach(function (value) {
                                    if (!value.id && value.name === receivedField.name) {
                                        value.id = receivedField.id;
                                    }
                                });
                            }
                        });
                    }
                });

                renderConfiguration(jobConfiguration, pipeline.id);
            }

            var groupBy = {};

            function addField(field) {
                var fieldSpan = $("<span>");
                fieldSpan.text(field.listName + ": ");
                fields.append(fieldSpan);
                var fieldValueSelect = $("<select>");
                if (field.multiValue) {
                    fieldValueSelect.attr('multiple', 'multiple');
                } else {
                    fieldValueSelect.append(new Option("(Not Specified)", -1));
                }
                fieldTypes[field.logicalListName].values.forEach(function (fieldValue) {
                    var selected = field.values.some(function (value) {
                        return value.id === fieldValue.id;
                    });
                    fieldValueSelect.append(new Option(fieldValue.name, fieldValue.id, selected));
                });
                fields.append(fieldValueSelect);
                enableDirtySelectCheck(fieldValueSelect);
                apply.push(function () {
                    field.values = [];
                    fieldValueSelect.find("option:selected").each(function (index, option) {
                        if (option.value < 0) {
                            // not specified
                        } else if (option.value == 0) {
                            // new value
                            field.values.push({
                                name: newValueInput.val()
                            });
                        } else {
                            field.values.push({
                                id: Number(option.value),
                                name: option.text
                            });
                        }
                    });
                });
                var newValueInput;
                if (field.extensible) {
                    var newValueOption = $(new Option("(New Value...)", 0));
                    fieldValueSelect.append(newValueOption);
                    newValueInput = $("<input>");
                    var validationArea = $("<div class='validation-error-area'>");
                    newValueInput.blur(validateInput(validationArea, newFieldValueValidation(newValueInput, fieldValueSelect)));
                    newValueInput.hide();
                    fields.append(newValueInput);
                    addInputWithValidation(newValueInput, fields, "Value must be specified", {
                        area: validationArea,
                        check: newFieldValueValidation(newValueInput, fieldValueSelect)
                    });

                    fieldValueSelect.change(function () {
                        validationArea.empty();
                        if (fieldValueSelect.val() == 0) {
                            newValueInput.css('display', 'inline');
                        } else {
                            newValueInput.hide();
                        }
                    });
                    fields.append(validationArea);
                }
                fields.append($("<br>"));
            }

            function addTag(tag) {

                var container;
                var group = groupBy[tag.tagTypeName];
                if (typeof group !== 'object') {
                    container = $("<div>");
                    var groupSpan = $("<span>");
                    groupSpan.text(tag.tagTypeName + ": ");
                    container.append(groupSpan);
                    group = {
                        target: container,
                        count: 0
                    };
                    groupBy[tag.tagTypeName] = group;
                    tags.append(container);
                }
                container = group.target;
                group.count++;

                var tagSpan = $("<span>");
                tagSpan.text(tag.tagName);
                container.append(tagSpan);

                var remove = $("<input type='button' value='X'>");
                enableDirtyButtonCheck(remove);
                remove.click(function () {
                    var index = pipeline.taxonomyTags.indexOf(tag);
                    pipeline.taxonomyTags.splice(index, 1);
                    tagSpan.remove();
                    remove.remove();
                    if (--group.count == 0) {
                        container.remove();
                        delete groupBy[tag.tagTypeName];
                    }
                    if (tag.tagId) {
                        addSelect.find("option[value='" + tag.tagId + "']").prop('disabled', false);
                    }
                });
                container.append(remove);
            }

            initialize();
            renderPipelineMetadata(pipeline);

            pipelineDiv.append("Fields: ").append($("<br>"));
            var fields = $("<div>");
            pipelineDiv.append(fields);
            pipeline.fieldTags.forEach(addField);

            pipelineDiv.append("Tags: ").append($("<br>"));
            var tags = $("<div>");
            pipelineDiv.append(tags);
            pipeline.taxonomyTags.forEach(addTag);

            var selectDiv = $("<div>");
            var addSelect = $("<select>");
            var defaultOption = new Option("Add Tag...", "default", true);
            $(defaultOption).prop('disabled', 'disabled');
            addSelect.append(defaultOption);
            jobConfiguration.taxonomies.forEach(function (tagType) {
                var group = $("<optgroup>");
                group.attr('label', tagType.tagTypeName);
                tagTypes[tagType.tagTypeId].values.forEach(function (tag) {
                    group.append(new Option(tag.tagName, tag.tagId));
                });
                group.append(new Option("New value...", -tagType.tagTypeId));
                addSelect.append(group);
            });
            var group = $("<optgroup>");
            group.attr('label', "New type...");
            group.append(new Option("New value...", 0));
            addSelect.append(group);
            var addedTag;
            addSelect.change(function () {
                var val = addSelect.val();
                if (val < 0) {
                    var tagType = tagTypes[-val];
                    addedTag = {
                        tagTypeId: tagType.tagTypeId,
                        tagTypeName: tagType.tagTypeName
                    };
                    tagTypeInput.val(tagType.tagTypeName);
                    tagTypeInput.hide();
                    tagTypeSpan.text(tagType.tagTypeName + ": ");
                    tagTypeSpan.css('display', 'inline');
                    tagInput.val("");
                    tagInput.attr('placeholder', 'Tag');
                    tagInput.css('display', 'inline');
                    add.css('display', 'inline');
                } else if (val == 0) {
                    addedTag = {};
                    tagTypeInput.val("");
                    tagTypeInput.attr('placeholder', 'Tag Type');
                    tagTypeInput.css('display', 'inline');
                    tagTypeSpan.hide();
                    tagInput.val("");
                    tagInput.attr('placeholder', 'Tag');
                    tagInput.css('display', 'inline');
                    add.css('display', 'inline');
                } else {
                    addedTag = allTags[val];
                    tagTypeInput.hide();
                    tagTypeSpan.hide();
                    tagInput.hide();
                    add.hide();
                    doAdd();
                }
                validationAreaTagType.empty();
                validationAreaTag.empty();
            });
            selectDiv.append(addSelect);
            pipelineDiv.append(selectDiv);

            pipeline.taxonomyTags.forEach(function (tag) {
                addSelect.find("option[value='"+tag.tagId+"']").prop('disabled', 'disabled');
            });

            var validationAreaTagType = $("<div class='validation-error-area'>");
            var validationAreaTag = $("<div class='validation-error-area'>");

            var tagTypeInput = $("<input type='text'>");
            tagTypeInput.hide();
            tagTypeInput.blur(validateInput(validationAreaTagType, newTagTypeValidation(tagTypeInput)));
            selectDiv.append(tagTypeInput);
            var tagTypeSpan = $("<span>");
            tagTypeSpan.hide();
            selectDiv.append(tagTypeSpan);
            var tagInput = $("<input type='text'>");
            tagInput.hide();
            tagInput.blur(validateInput(validationAreaTag, newTagValidation(tagTypeInput, tagInput, pipeline.taxonomyTags)));
            selectDiv.append(tagInput);

            var add = $("<input type='button' value='Add'>");
            add.hide();
            var doAdd = function () {
                var validationOk = true;
                if (!addedTag.tagTypeId) {
                    addedTag.tagTypeName = tagTypeInput.val();
                    if (!validateInput(validationAreaTagType, newTagTypeValidation(tagTypeInput))()) {
                        validationOk = false;
                    }
                }
                if (!addedTag.tagId) {
                    addedTag.tagName = tagInput.val();
                    if (!validateInput(validationAreaTag, newTagValidation(tagTypeInput, tagInput, pipeline.taxonomyTags))()) {
                        validationOk = false;
                    }
                }
                if (!validationOk) {
                    return;
                }
                pipeline.taxonomyTags.push(addedTag);
                addTag(addedTag);
                if (addedTag.tagId) {
                    addSelect.find("option:selected").prop('disabled', 'disabled');
                }
                addedTag = undefined;
                makeDirty();
                $(defaultOption).prop('selected', 'selected');
                tagTypeInput.hide();
                tagTypeSpan.hide();
                tagInput.hide();
                add.hide();
            };
            add.click(doAdd);
            enableDirtyButtonCheck(add);
            selectDiv.append(add);

            // put validation area bellow both input fields
            selectDiv.append(validationAreaTagType);
            selectDiv.append(validationAreaTag);

            addApplyButton('Apply', pipeline, saveFunc, saveCallback);
        }

        var CONFIRMATION = "There are unsaved changes, if you continue they will be discarded. Continue?";
        var pipelineSelect;

        if (jobConfiguration.pipelines.length == 0) {
            var createPipelineDiv = $("<div>No pipeline is currently defined for this job<br/></div>");
            var createPipelineButton = $("<input type='button' value='Create Pipeline'>");
            createPipelineButton.click(function () {
                pipelineDiv.empty();
                var pipeline = {
                    id: null,
                    isRoot: true,
                    fieldTags: [],
                    taxonomyTags: []
                };
                jobConfiguration.pipelines.push(pipeline);

                renderNewPipeline(pipeline);
            });
            createPipelineDiv.append(createPipelineButton);
            pipelineDiv.append(createPipelineDiv);
        } else {
            var selectedIndex = 0;
            if (jobConfiguration.pipelines.length > 1) {
                pipelineSelect = $("<select>");
                jobConfiguration.pipelines.forEach(function (pipeline) {
                    pipelineSelect.append(new Option(pipeline.name, pipeline.id, (pipeline.id === pipelineId)));
                });
                var lastSelected = $(pipelineSelect).find("option:selected");
                pipelineSelect.change(function () {
                    if (isDirty()) {
                        if (!window.confirm(CONFIRMATION)) {
                            lastSelected.attr("selected", true);
                            return;
                        }
                    }
                    lastSelected = $(pipelineSelect).find("option:selected");
                    renderExistingPipeline(jobConfiguration.pipelines[pipelineSelect[0].selectedIndex]);
                });
                result.prepend(pipelineSelect);
                selectedIndex = pipelineSelect[0].selectedIndex;
            }
            renderExistingPipeline(jobConfiguration.pipelines[selectedIndex]);
        }

        window.onbeforeunload = function() {
            if (isDirty()) {
                return CONFIRMATION;
            } else {
                // keep original check just in case there is another dirty data (shouldn't be)
                if (typeof originalUnload === 'function') {
                    return originalUnload();
                } else {
                    return undefined;
                }
            }
        };

        function makeDirty() {
            dirtyFlag = true;
        }

        function clearDirty() {
            dirtyFlag = false;
        }

        function isDirty() {
            return dirtyFlag;
        }

        function enableDirtySelectCheck(select) {
            select.on('change', makeDirty);
        }

        function enableDirtyInputCheck(input) {
            input.on('input', makeDirty);
        }

        function enableDirtyButtonCheck(button) {
            button.on('click', makeDirty);
        }

        function validateFields() {
            var valid = true;
            validators.forEach(function (validator) {
                if (!validator()) {
                    valid = false;
                }
            });
            return valid;
        }

        function applyFields() {
            apply.forEach(function (func) {
                func();
            });
        }

        function newTagValidation(tagTypeInput, tagInput, taxonomyTags) {
            return function () {
                var error = undefined;

                function matchTag(tag) {
                    if (caseInsensitiveStringEquals(tag.tagName, tagInput.val())) {
                        error = "Tag " + tagType.tagTypeName + ":" + tag.tagName + " is already defined";
                        return true;
                    } else {
                        return false;
                    }
                }

                function matchAddedTag(tag) {
                    if (caseInsensitiveStringEquals(tag.tagName, tagInput.val()) &&
                            caseInsensitiveStringEquals(tag.tagTypeName, tagTypeInput.val())) {
                        error = "Tag " + tag.tagTypeName + ":" + tag.tagName + " is already added";
                        return true;
                    } else {
                        return false;
                    }
                }

                if (!tagInput.val()) {
                    return "Tag must be specified";
                }

                var tagType = tagTypesByName[tagTypeInput.val()];
                if (tagType) {
                    tagType.values.some(matchTag);
                }

                if (!error) {
                    // could be added as new tag
                    taxonomyTags.some(matchAddedTag);
                }

                return error;
            };
        }

        function newFieldValueValidation(newValueInput, valueSelect) {
            return function () {
                var error = undefined;

                function matchValue(item) {
                    if (caseInsensitiveStringEquals(item, newValueInput.val())) {
                        error = "Value " + item + " is already defined";
                        return true;
                    } else {
                        return false;
                    }
                }

                if (valueSelect.val() != '0') {
                    return;
                }

                if (!newValueInput.val()) {
                    return "Value must be specified";
                }

                var values = [];
                valueSelect.find("option").each(function (index, option) {
                    values.push(option.text);
                });

                values.some(matchValue);
                return error;
            };
        }

        function newTagTypeValidation(tagTypeInput) {
            return function () {
                var error = undefined;

                function matchTagType(tagType) {
                    if (caseInsensitiveStringEquals(tagType.tagTypeName, tagTypeInput.val())) {
                        error = "Tag Type " + tagType.tagTypeName + " is already defined";
                        return true;
                    } else {
                        return false;
                    }
                }

                if (!tagTypeInput.val()) {
                    return "Tag type must be specified";
                }

                jobConfiguration.taxonomies.some(matchTagType);
                return error;
            };
        }

        function addInputWithValidation(input, target, message, options_opt) {
            function emptyCheck() {
                if (!input.val()) {
                    return message;
                } else {
                    return false;
                }
            }
            var options = options_opt || {};
            var check = options.check || emptyCheck;
            var validationArea = options.area;
            target.append(input);
            if (!validationArea) {
                validationArea = $("<div class='validation-error-area'>");
                target.append(validationArea);
            }
            var validate = validateInput(validationArea, check);
            input.blur(validate);
            validators.push(validate);
            validationArea.hide();
        }

        function validateInput(target, conditionFunc) {

            function showError(message) {
                var container = $("<div class='error'/>");
                container.html(message);
                target.append(container);
                target.show();
            }

            return function() {
                target.empty();
                var error = conditionFunc();
                if (error) {
                    showError(error);
                    return false;
                }
                target.hide();
                return true;
            };
        }

        function validationError(error) {
            var errorDiv = $("<div class='error'><font color='red'><b/></font></div>");
            errorDiv.find("b").text(error);
            status.append(errorDiv);
        }

        function saveConfiguration(pipeline, saveFunc, saveCallback) {
            if (!validateFields()) {
                return;
            }
            applyFields();

            status.empty();

            progressFunc("Storing configuration on server");
            saveFunc(pipeline, function (t) {
                progressFunc();
                var response = t.responseObject();
                if (response.errors) {
                    response.errors.forEach(validationError);
                } else {
                    saveCallback(pipeline, response);
                    clearDirty();
                }
            });
        }
    }

    return {
        configure: configure
    };
}