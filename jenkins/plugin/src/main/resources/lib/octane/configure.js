function octane_job_configuration(target, progress, proxy) {

    if (typeof jQuery === 'undefined') {
        return {
            configure: function() {
                target.innerHTML = "JQuery plugin must be installed and enabled <a href=" + rootURL + "/pluginManager>Plugin Manager</a>";
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

        var applyButton = $("<input type='button' value='Apply'>");
        buttons.append(applyButton);

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

        var validators = [];
        var apply = [];
        var dirty = [];

        function renderPipeline(pipeline, saveFunc, saveCallback) {

            var groupBy = {};

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
                remove.click(function () {
                    var index = pipeline.taxonomyTags.indexOf(tag);
                    pipeline.taxonomyTags.splice(index, 1);
                    dirty.push(function () {
                        return true; // tag was removed
                    });
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

            validators.length = 0;
            apply.length = 0;
            dirty.length = 0;

            applyButton.unbind('click').click(function() {
                saveConfiguration(pipeline, saveFunc, saveCallback);
            });

            pipelineDiv.empty();
            pipelineDiv.append("Pipeline: ");
            if (pipeline.isRoot) {
                var input = $("<input type='text' placeholder='Pipeline name'>");
                input.attr("value", pipeline.name);
                apply.push(function() {
                    pipeline.name = input.val();
                });
                dirty.push(function () {
                    return pipeline.name !== input.val();
                });
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
                dirty.push(function () {
                    return pipeline.releaseId != select.val();
                });
                pipelineDiv.append(select).append($("<br>"));
            }

            pipelineDiv.append("Tags: ").append($("<br>"));
            var tags = $("<div>");
            pipelineDiv.append(tags);
            pipeline.taxonomyTags.forEach(addTag);

            pipelineDiv.append(buttons);

            var selectDiv = $("<div>");
            var addSelect = $("<select>");
            var defaultOption = new Option("Choose...", "default", true);
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
                add.prop('disabled', false);
                var val = addSelect.val();
                if (val < 0) {
                    var tagType = tagTypes[-val];
                    addedTag = {
                        tagTypeId: tagType.tagTypeId,
                        tagTypeName: tagType.tagTypeName
                    };
                    tagTypeInput.val(tagType.tagTypeName);
                    tagTypeInput.prop('disabled', 'disabled');
                    tagTypeInput.css('display', 'inline');
                    tagInput.val("");
                    tagInput.attr('placeholder', 'Tag');
                    tagInput.css('display', 'inline');
                } else if (val == 0) {
                    addedTag = {};
                    tagTypeInput.val("");
                    tagTypeInput.attr('placeholder', 'Tag Type');
                    tagTypeInput.prop('disabled', false);
                    tagTypeInput.css('display', 'inline');
                    tagInput.val("");
                    tagInput.attr('placeholder', 'Tag');
                    tagInput.css('display', 'inline');
                } else {
                    addedTag = allTags[val];
                    tagTypeInput.hide();
                    tagInput.hide();
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
            var tagInput = $("<input type='text'>");
            tagInput.hide();
            tagInput.blur(validateInput(validationAreaTag, newTagValidation(tagTypeInput, tagInput, pipeline.taxonomyTags)));
            selectDiv.append(tagInput);

            var add = $("<input type='button' value='Add'>");
            add.prop('disabled', 'disabled');
            add.click(function () {
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
                dirty.push(function () {
                    return true; // there is new tag
                });
                $(defaultOption).prop('selected', 'selected');
                tagTypeInput.hide();
                tagInput.hide();
            });
            selectDiv.append(add);

            // put validation area bellow both input fields
            selectDiv.append(validationAreaTagType);
            selectDiv.append(validationAreaTag);
        }

        var CONFIRMATION = "There are unsaved changes, if you continue they will be discarded. Continue?";
        var pipelineSelect;
        var saveFunc, saveCallback;

        if (jobConfiguration.pipelines.length == 0) {
            saveFunc = function (pipeline, callback) {
                proxy.createPipelineOnServer(pipeline, callback);
            };
            saveCallback = function (pipeline, response) {
                pipeline.id = response.id;
                renderConfiguration(jobConfiguration, pipeline.id);
            };
            var createPipelineDiv = $("<div>No pipeline is currently defined for this job<br/></div>");
            var createPipelineButton = $("<input type='button' value='Create Pipeline'>");
            createPipelineButton.click(function () {
                pipelineDiv.empty();
                var pipeline = {
                    id: null,
                    isRoot: true,
                    taxonomyTags: []
                };
                jobConfiguration.pipelines.push(pipeline);
                renderPipeline(pipeline, saveFunc, saveCallback);
            });
            createPipelineDiv.append(createPipelineButton);
            pipelineDiv.append(createPipelineDiv);
        } else {
            saveFunc = function (pipeline, callback) {
                proxy.updatePipelineOnSever(pipeline, callback);
            };
            saveCallback = function (pipeline, response) {
                pipeline.taxonomyTags = response.taxonomies;

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

                renderConfiguration(jobConfiguration, pipeline.id);
            };
            var selectedIndex = 0;
            if (jobConfiguration.pipelines.length > 1) {
                pipelineSelect = $("<select>");
                jobConfiguration.pipelines.forEach(function (pipeline) {
                    pipelineSelect.append(new Option(pipeline.name, pipeline.id, (pipeline.id === pipelineId)));
                });
                var lastSelected = $(pipelineSelect).find("option:selected");
                pipelineSelect.change(function () {
                    if (dirtyFields()) {
                        if (!window.confirm(CONFIRMATION)) {
                            lastSelected.attr("selected", true);
                            return;
                        }
                    }
                    lastSelected = $(pipelineSelect).find("option:selected");
                    renderPipeline(jobConfiguration.pipelines[pipelineSelect[0].selectedIndex], saveFunc, saveCallback);
                });
                result.prepend(pipelineSelect);
                selectedIndex = pipelineSelect[0].selectedIndex;
            }
            renderPipeline(jobConfiguration.pipelines[selectedIndex], saveFunc, saveCallback);
        }

        window.onbeforeunload = function() {
            if (dirtyFields()) {
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

        function dirtyFields() {
            return dirty.some(function (func) {
                return func()
            });
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

        function addInputWithValidation(input, target, message) {
            function emptyCheck() {
                if (!input.val()) {
                    return message;
                } else {
                    return false;
                }
            }
            target.append(input);
            var validationArea = $("<div class='validation-error-area'>");
            target.append(validationArea);
            var validate = validateInput(validationArea, emptyCheck);
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
                }
            });
        }
    }

    return {
        configure: configure
    };
}