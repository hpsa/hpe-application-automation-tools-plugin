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
        result.append(buttons);

        var status = $("<div>");
        buttons.append(status);

        var applyButton = $("<input type='button' value='Apply'>");

        var tagTypes = {};
        jobConfiguration.taxonomies.forEach(function (tagType) {
            tagTypes[tagType.tagTypeId] = tagType;
        });

        var validators = [];
        var apply = [];
        var dirty = [];

        function renderPipeline(pipeline, saveFunc, saveCallback) {

            function firstTag() {
                if (jobConfiguration.taxonomies.length > 0) {
                    var firstTagType = jobConfiguration.taxonomies[0];
                    if (firstTagType.values.length > 0) {
                        var firstTag = firstTagType.values[0];
                        return {
                            tagTypeId: firstTagType.tagTypeId,
                            tagTypeName: firstTagType.tagTypeName,
                            tagId: firstTag.tagId,
                            tagName: firstTag.tagName
                        };
                    }
                }
                return undefined;
            }

            function firstTagType() {
                if (jobConfiguration.taxonomies.length > 0) {
                    var firstTagType = jobConfiguration.taxonomies[0];
                    return {
                        tagTypeId: firstTagType.tagTypeId,
                        tagTypeName: firstTagType.tagTypeName
                    };
                }
                return undefined;
            }

            var firstTagValue = firstTag();
            var firstTagTypeValue = firstTagType();

            function addTag(tag) {
                var tagTypeSelect, tagSelect;
                var tagTypeEdit, tagEdit;

                function loadTagValues() {
                    tagSelect.empty();
                    var tagTypeId = tagTypeSelect.val();
                    if (tagTypeId) {
                        tagTypes[tagTypeId].values.forEach(function (tag) {
                            tagSelect.append(new Option(tag.tagName, tag.tagId));
                        });
                    }
                }

                var container = $("<div>");
                tags.append(container);

                if (tag.tagTypeId) {
                    tagTypeSelect = $("<select>");
                    jobConfiguration.taxonomies.forEach(function (tagType) {
                        tagTypeSelect.append(new Option(tagType.tagTypeName, tagType.tagTypeId));
                    });
                    tagTypeSelect.val(tag.tagTypeId);
                    tagTypeSelect.change(loadTagValues);
                    container.append(tagTypeSelect);
                    apply.push(function () {
                        tag.tagTypeId = tagTypeSelect.val();
                        tag.tagTypeName = tagTypeSelect.find("option:selected").text();
                    });
                } else {
                    tagTypeEdit = $("<input type='text' placeholder='Tag Type'>");
                    var validationAreaTagType = $("<div class='validation-error-area'>");
                    addInputWithValidation(tagTypeEdit, container, "Tag Type name must be specified", {
                        "area": validationAreaTagType,
                        "validate": newTagTypeValidation(tagTypeEdit)
                    });
                    apply.push(function () {
                        tag.tagTypeId = undefined;
                        tag.tagTypeName = tagTypeEdit.val();
                    });
                    dirty.push(function () {
                        return true;
                    });
                }

                if (tag.tagId) {
                    tagSelect = $("<select>");
                    loadTagValues();
                    tagSelect.val(tag.tagId);
                    container.append(tagSelect);
                    apply.push(function () {
                        tag.tagId = tagSelect.val();
                        tag.tagName = tagSelect.find("option:selected").text();
                    });
                    dirty.push(function () {
                        return tag.tagId !== tagSelect.val();
                    });
                } else {
                    tagEdit = $("<input type='text' placeholder='Tag'>");
                    var validationAreaTag = $("<div class='validation-error-area'>");
                    addInputWithValidation(tagEdit, container, "Tag name must be specified", {
                        "area": validationAreaTag,
                        "validate": newTagValidation(tagTypeSelect, tagEdit)
                    });
                    apply.push(function () {
                        tag.tagId = undefined;
                        tag.tagName = tagEdit.val();
                    });
                }

                // put validation area bellow both input fields
                container.append(validationAreaTagType);
                container.append(validationAreaTag);

                var remove = $("<input type='button' value='Remove'>");
                remove.click(function () {
                    var index = pipeline.taxonomyTags.indexOf(tag);
                    pipeline.taxonomyTags.splice(index, 1);
                    dirty.push(function () {
                        return true; // tag was removed
                    });
                    container.remove();
                });
                container.append(remove);
                container.append($("<br>")).append($("<hr width='400' align='left'>"));
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

            var addKindSelect = $("<select>");
            if (firstTagValue) {
                addKindSelect.append(new Option("Existing Tag", 0));
            }
            if (firstTagTypeValue) {
                addKindSelect.append(new Option("New Tag", 1));
            }
            addKindSelect.append(new Option("New Tag Type", 2));
            pipelineDiv.append(addKindSelect);

            var add = $("<input type='button' value='Add'>");
            add.click(function () {
                var first;
                switch (addKindSelect.val()) {
                    case '0':
                        first = firstTag();
                        break;
                    case '1':
                        first = firstTagType();
                        break;
                    case '2':
                        first = {};

                }
                pipeline.taxonomyTags.push(first);
                addTag(first);
                dirty.push(function () {
                    return true; // there is new tag
                });
            });
            pipelineDiv.append(add);
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
                buttons.append(applyButton);
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
            buttons.append(applyButton);
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

        function newTagValidation(tagTypeSelect, tagInput) {
            return function () {
                var error = undefined;

                function matchTag(tag) {
                    if (caseInsensitiveStringEquals(tag.tagName, tagInput.val())) {
                        error = "Tag " + tagTypes[tagTypeSelect.val()].tagTypeName + ":" + tag.tagName + " is already defined";
                        return true;
                    } else {
                        return false;
                    }
                }

                if (tagTypeSelect) {
                    var tagType = tagTypes[tagTypeSelect.val()];
                    tagType.values.some(matchTag);
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

                jobConfiguration.taxonomies.some(matchTagType);
                return error;
            };
        }

        function addInputWithValidation(input, target, message, configOpt) {
            var config = configOpt || {};
            target.append(input);
            var validationArea = config.area;
            if (!validationArea) {
                validationArea = $("<div class='validation-error-area'>");
                target.append(validationArea);
            }
            var validate = validateInput(input, validationArea, message, config.validate);
            input.blur(validate);
            validators.push(validate);
            validationArea.hide();
        }

        function validateInput(input, target, message, validateFuncOpt) {

            function showError(message) {
                var container = $("<div class='error'/>");
                container.html(message);
                target.append(container);
                target.show();
            }

            return function() {
                target.empty();
                if (input) {
                    if (!input.val()) {
                        showError(message);
                        return false;
                    } else if (validateFuncOpt) {
                        var errorMessage = validateFuncOpt();
                        if (errorMessage) {
                            showError(errorMessage);
                            return false;
                        }
                    }
                }
                target.hide();
                return true;
            };
        }

        function getTagDuplicates(pipeline) {
            var existing = {};
            var duplicates = [];

            function addDuplicate(tag) {
                duplicates.push('Tag ' + tag.tagTypeName + ':' + tag.tagName + ' specified multiple times');
            }

            pipeline.taxonomyTags.forEach(function (tag) {
                if (tag.tagId) {
                    if (existing[tag.tagId]) {
                        // matching existing tag
                        addDuplicate(tag);
                        return;
                    }
                    existing[tag.tagId] = true;
                    return;
                }
                if (tag.tagTypeId) {
                    var newTagKey = (String(tag.tagTypeId) + '#' + tag.tagName);
                    if (existing[newTagKey]) {
                        // matching new tag
                        addDuplicate(tag);
                        return;
                    }
                    existing[newTagKey] = true;
                    return;
                }
                var newTypeKey = (tag.tagTypeName + '#' + tag.tagName);
                if (existing[newTypeKey]) {
                    // matching new tag type
                    addDuplicate(tag);
                    return;
                }
                existing[newTypeKey] = true;
            });

            return duplicates;
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

            var duplicates = getTagDuplicates(pipeline);
            if (duplicates.length > 0) {
                duplicates.forEach(validationError);
                return;
            }

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