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

        var pipelineDiv = $("<div class='mqm'>");
        result.append(pipelineDiv);

        var buttons = $("<div>");
        var status = $("<div>");

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
            buttons.empty();
            buttons.append(status);
            var applyButton = $("<button>");
            applyButton.text(caption);
            applyButton.unbind('click').click(function() {
                saveConfiguration(pipeline, func, callback);
            });
            buttons.append(applyButton);
            pipelineDiv.append(buttons);
        }

        function renderPipelineMetadata(pipeline, pipelineSelector) {
            var table = $("<table class='ui-block'><tbody><tr/></tbody></table>");
            pipelineDiv.append(table);
            var tbody = table.find("tbody");
            var tr = tbody.find("tr");

            if (pipelineSelector) {
                // put the pipeline selector at the top of the table
                tbody.prepend(pipelineSelector.tr);
                // need to reapply event listener
                pipelineSelector.apply();
            }

            var tdPipeline = $("<td class='setting-name'><label for='pipeline-name'>Pipeline:</label></td>");
            tr.append(tdPipeline);

            if (pipeline.isRoot) {
                var tdPipelineInput = $("<td class='setting-main' colspan='2'><input id='pipeline-name' type='text' placeholder='Pipeline name' class='setting-input'/><div class='validation-error-area'/></td>");
                tr.append(tdPipelineInput);

                var input = tdPipelineInput.find("input");
                input.attr("value", pipeline.name);
                var area = tdPipelineInput.find("div");

                apply.push(function() {
                    pipeline.name = input.val();
                });
                enableDirtyInputCheck(input);
                enableInputValidation(input, "Pipeline name must be specified", area);
            } else {
                var tdPipelineName = $("<td class='setting-main' colspan='2'/>");
                tr.append(tdPipelineName);

                tdPipelineName.append(pipeline.name);
            }

            if (pipeline.isRoot) {
                var trRelease = $("<tr><td class='setting-name'><label for='pipeline-release'>Release:</label>");
                tbody.append(trRelease);

                var tdReleaseSelect = $("<td class='setting-main'>");
                trRelease.append(tdReleaseSelect);

                var select = $("<select>");
                for (var releaseId in jobConfiguration.releases) {
                    select.append($("<option>").text(jobConfiguration.releases[releaseId]).val(releaseId).attr('selected', (pipeline.releaseId === releaseId)));
                }
                apply.push(function () {
                    pipeline.releaseId = select.val();
                });
                enableDirtyChangeCheck(select);
                tdReleaseSelect.append(select);

                trRelease.append($("<td class='setting-new'/>"));
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

        function renderExistingPipeline(pipeline, pipelineSelector) {

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
                var tr = $("<tr>");
                fieldsTbody.append(tr);

                var tdName = $("<td class='setting-name'>");
                tr.append(tdName);

                var label = $("<label>");
                label.prop('for', field.logicalListName);
                label.text(field.listName + ":");
                tdName.append(label);

                var tdSelect = $("<td class='setting-main'>");
                tr.append(tdSelect);

                var fieldValueSelect = $("<select>");
                tdSelect.append(fieldValueSelect);

                fieldValueSelect.prop('name', field.logicalListName);
                fieldValueSelect.prop('id', field.logicalListName);
                if (field.multiValue) {
                    fieldValueSelect.attr('multiple', 'multiple');
                    tdSelect.prop('colspan', 2);
                } else {
                    fieldValueSelect.append($("<option value='-1'>-- Not specified --</option>"));
                }
                fieldTypes[field.logicalListName].values.forEach(function (fieldValue) {
                    var selected = field.values.some(function (value) {
                        return value.id === fieldValue.id;
                    });
                    fieldValueSelect.append($("<option>").text(fieldValue.name).val(fieldValue.id).attr('selected', selected));
                });
                enableDirtyChangeCheck(fieldValueSelect);
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
                if (field.extensible) {
                    var newValueOption = $("<option value='0'>New value...</option>");
                    fieldValueSelect.append(newValueOption);

                    var tdAdd = $("<td class='setting-add'>");
                    tr.append(tdAdd);

                    var newValueInput = $("<input type='text' class='setting-input'>");
                    tdAdd.append(newValueInput);

                    var trArea = $("<tr><td/></tr>");
                    fieldsTbody.append(trArea);

                    var tdArea = $("<td class='setting-main' colspan='2'>");
                    trArea.append(tdArea);

                    var validationArea = $("<div class='validation-error-area'>");
                    tdArea.append(validationArea);

                    newValueInput.blur(validateInput(validationArea, newFieldValueValidation(newValueInput, fieldValueSelect)));
                    newValueInput.hide();
                    enableInputValidation(newValueInput, "Value must be specified", validationArea, {
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
                }
            }

            function addTag(tag) {

                var tagTd;
                var group = groupBy[tag.tagTypeName];
                if (typeof group !== 'object') {
                    var tagTr = $("<tr><td class='setting-name'><label/></td>");
                    tagsTbody.append(tagTr);
                    tagTr.find("label").text(tag.tagTypeName + ":");
                    tagTd = $("<td class='setting-main' colspan='2'/>");
                    tagTr.append(tagTd);
                    group = {
                        tr: tagTr,
                        td: tagTd,
                        count: 0
                    };
                    groupBy[tag.tagTypeName] = group;
                }
                tagTd = group.td;
                group.count++;

                var tagDiv = $("<div class='tag'><span/><a class='remove' href='javascript:void(0)'>X</a></div>");
                tagTd.append(tagDiv).append(" ");
                tagDiv.find("span").text(tag.tagName);
                var anchor = tagDiv.find("a");
                enableDirtyClickCheck(anchor);
                anchor.on("click", function() {
                    var index = pipeline.taxonomyTags.indexOf(tag);
                    pipeline.taxonomyTags.splice(index, 1);
                    tagDiv.remove();
                    if (--group.count == 0) {
                        tagTr.remove();
                        delete groupBy[tag.tagTypeName];
                    }
                    if (tag.tagId) {
                        addSelect.find("option[value='" + tag.tagId + "']").prop('disabled', false);
                    }
                });
            }

            initialize();
            renderPipelineMetadata(pipeline, pipelineSelector);

            pipelineDiv.append($("<h4>Fields</h4>"));
            var fieldsTable = $("<table class='ui-block'><tbody/></table>");
            pipelineDiv.append(fieldsTable);
            var fieldsTbody = fieldsTable.find("tbody");
            pipeline.fieldTags.forEach(addField);

            pipelineDiv.append($("<h4>Tags</h4>"));
            var tagsTable = $("<table class='ui-block'><tbody/></table>");
            pipelineDiv.append(tagsTable);
            var tagsTbody = tagsTable.find("tbody");
            pipeline.taxonomyTags.forEach(addTag);

            var addTagTable = $("<table class='ui-block'><tbody/></table>");
            pipelineDiv.append(addTagTable);
            var addTagTbody = addTagTable.find("tbody");

            var tagSelectTr = $("<tr>");
            addTagTbody.append(tagSelectTr);
            var tagSelectTd = $("<td>");
            tagSelectTr.append(tagSelectTd);
            var addSelect = $("<select>");
            tagSelectTd.append(addSelect);
            var defaultOption = $("<option value='default' selected>Add Tag...</option>");
            defaultOption.prop('disabled', 'disabled');
            addSelect.append(defaultOption);
            jobConfiguration.taxonomies.forEach(function (tagType) {
                var group = $("<optgroup>");
                group.attr('label', tagType.tagTypeName);
                tagTypes[tagType.tagTypeId].values.forEach(function (tag) {
                    group.append($("<option>").text(tag.tagName).val(tag.tagId));
                });
                group.append($("<option>New value...</option>").val(-tagType.tagTypeId));
                addSelect.append(group);
            });
            var group = $("<optgroup>");
            group.attr('label', "New type...");
            group.append($("<option value='0'>New value...</option>"));
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

            pipeline.taxonomyTags.forEach(function (tag) {
                addSelect.find("option[value='"+tag.tagId+"']").prop('disabled', 'disabled');
            });

            var validationAreaTagType = $("<div class='validation-error-area'>");
            var validationAreaTag = $("<div class='validation-error-area'>");

            var tagTypeInputTd = $("<td class='setting-name'>");
            tagSelectTr.append(tagTypeInputTd);
            var tagTypeInput = $("<input type='text' class='setting-input'>");
            tagTypeInputTd.append(tagTypeInput);
            tagTypeInput.hide();
            tagTypeInput.blur(validateInput(validationAreaTagType, newTagTypeValidation(tagTypeInput)));
            var tagTypeSpan = $("<span>");
            tagTypeSpan.hide();
            tagTypeInputTd.append(tagTypeSpan);

            var tagInputTd = $("<td>");
            tagSelectTr.append(tagInputTd);
            var tagInput = $("<input type='text' class='setting-input'>");
            tagInputTd.append(tagInput);
            tagInput.hide();
            tagInput.blur(validateInput(validationAreaTag, newTagValidation(tagTypeInput, tagInput, pipeline.taxonomyTags)));

            var add = $("<button>Add</button>");
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
                defaultOption.prop('selected', 'selected');
                tagTypeInput.hide();
                tagTypeSpan.hide();
                tagInput.hide();
                add.hide();
            };
            add.click(doAdd);
            enableDirtyClickCheck(add);
            var tagAddTd = $("<td>");
            tagSelectTr.append(tagAddTd);
            tagAddTd.append(add);

            // put validation area bellow both input fields
            var tagValidationTr = $("<tr>");
            addTagTbody.append(tagValidationTr);
            tagValidationTr.append($("<td>"));
            var tagValidationTd = $("<td colspan='2'>");
            tagValidationTr.append(tagValidationTd);

            tagValidationTd.append(validationAreaTagType);
            tagValidationTd.append(validationAreaTag);

            addApplyButton('Apply', pipeline, saveFunc, saveCallback);
        }

        var CONFIRMATION = "There are unsaved changes, if you continue they will be discarded. Continue?";

        if (jobConfiguration.pipelines.length == 0) {
            var createPipelineDiv = $("<div><h2>No Pipeline</h2><div class='mqm'><p>No pipeline is currently defined for this job</p><button>Create Pipeline</button></div></div>");
            pipelineDiv.append(createPipelineDiv);
            var createPipelineButton = createPipelineDiv.find("button");
            createPipelineDiv.find("div.mqm").append(createPipelineButton);
            createPipelineButton.click(function () {
                pipelineDiv.empty();
                var pipeline = {
                    id: null,
                    isRoot: true,
                    fieldTags: [],
                    taxonomyTags: []
                };
                jobConfiguration.pipelines.push(pipeline);

                result.prepend($("<h2>Create Pipeline</h2>"));
                renderNewPipeline(pipeline);
            });
        } else {
            var selectedIndex = 0;
            var pipelineSelector = undefined;
            if (jobConfiguration.pipelines.length > 1) {
                var selectPipelineRow = $("<tr><td/><td class='setting-main'><select/><br/><br/></td><td class='setting-new'></td></tr>");
                var pipelineSelect = selectPipelineRow.find("select");
                jobConfiguration.pipelines.forEach(function (pipeline) {
                    pipelineSelect.append($("<option>").text(pipeline.name).val(pipeline.id).attr('selected', (pipeline.id === pipelineId)));
                });
                var lastSelected = $(pipelineSelect).find("option:selected");
                pipelineSelector = {
                    tr: selectPipelineRow,
                    apply: function () {
                        pipelineSelect.change(function () {
                            if (isDirty()) {
                                if (!window.confirm(CONFIRMATION)) {
                                    lastSelected.attr("selected", true);
                                    return;
                                }
                            }
                            lastSelected = $(pipelineSelect).find("option:selected");
                            renderExistingPipeline(jobConfiguration.pipelines[pipelineSelect[0].selectedIndex], pipelineSelector);
                        });
                    }
                };
                selectedIndex = pipelineSelect[0].selectedIndex;
            }
            result.prepend($("<h2>Edit Pipeline</h2>"));
            renderExistingPipeline(jobConfiguration.pipelines[selectedIndex], pipelineSelector);
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

        function enableDirtyChangeCheck(select) {
            select.on('change', makeDirty);
        }

        function enableDirtyInputCheck(input) {
            input.on('input', makeDirty);
        }

        function enableDirtyClickCheck(button) {
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

        function enableInputValidation(input, message, validationArea, options_opt) {
            function emptyCheck() {
                if (!input.val()) {
                    return message;
                } else {
                    return false;
                }
            }
            var options = options_opt || {};
            var check = options.check || emptyCheck;
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