function octane_job_configuration(target, progress, proxy) {

    if (typeof jQuery === 'undefined') {
        return {
            configure: function() {
                target.innerHTML = "JQuery plugin must be installed and enabled <a href=" + rootURL + "/pluginManager>Plugin Manager</a>";
            }
        }
    }

    var $ = jQuery;
    function configure() {
        progressFunc("Retrieving configuration from server");
        proxy.loadJobConfigurationFromServer(function (t) {
            progressFunc();
            var jobConfiguration = t.responseObject();
            renderConfiguration(jobConfiguration);
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
        jobConfiguration.availableTags.forEach(function (tagType) {
            tagTypes[tagType.tagTypeId] = tagType;
        });

        var validators = [];
        var apply = [];
        var dirty = [];

        function renderPipeline(pipeline) {

            function firstTag() {
                if (jobConfiguration.availableTags.length > 0) {
                    var firstTagType = jobConfiguration.availableTags[0];
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

            function addTag(tag) {
                var tagTypeSelect, tagSelect;

                function loadTagValues() {
                    tagSelect.empty();
                    var tagTypeId = tagTypeSelect.val();
                    tagTypes[tagTypeId].values.forEach(function (tag) {
                        tagSelect.append(new Option(tag.tagName, tag.tagId));
                    });
                }

                var container = $("<div>");
                tags.append(container);

                tagTypeSelect = $("<select>");
                jobConfiguration.availableTags.forEach(function (tagType) {
                    tagTypeSelect.append(new Option(tagType.tagTypeName, tagType.tagTypeId));
                });
                tagTypeSelect.val(tag.tagTypeId);
                tagTypeSelect.change(loadTagValues);
                container.append(tagTypeSelect);

                tagSelect = $("<select>");
                loadTagValues();
                tagSelect.val(tag.tagId);
                container.append(tagSelect);

                apply.push(function () {
                    tag.tagId = tagSelect.val();
                    tag.tagName = $(tagSelect).find("option:selected").text();
                    tag.tagTypeId = tagTypeSelect.val();
                    tag.tagTypeName = $(tagTypeSelect).find("option:selected").text();
                });
                dirty.push(function () {
                    return tag.tagId !== tagSelect.val();
                });

                var remove = $("<input type='button' value='Remove'>");
                var index = container.index();
                remove.click(function () {
                    apply.push(function () {
                        pipeline.tags[index] = null;
                    });
                    dirty.push(function () {
                        return true; // tag was removed
                    });
                    container.remove();
                });
                container.append(remove);
                container.append($("<br>"));
            }

            validators.length = 0;
            apply.length = 0;
            dirty.length = 0;

            applyButton.unbind('click').click(function() {
                saveConfiguration(pipeline);
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
                pipelineDiv.append(input);
                var validationArea = $("<div class='validation-error-area'>");
                var validate = function() {
                    if (!input.val()) {
                        validationArea.html("<div class='error'>Pipeline name must be specified</div>");
                        validationArea.show();
                        return false;
                    } else {
                        validationArea.empty();
                        validationArea.hide();
                        return true;
                    }
                };
                input.blur(validate);
                validators.push(validate);
                validationArea.hide();
                pipelineDiv.append(validationArea);
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
                    return pipeline.releaseId !== select.val();
                });
                pipelineDiv.append(select).append($("<br>"));
            } else {
                var noPush = $("<input type='checkbox'>");
                apply.push(function () {
                    pipeline.noPush = noPush.prop('checked');
                });
                dirty.push(function () {
                    return pipeline.noPush !== noPush.prop('checked');
                });
                pipelineDiv.append(noPush).append("Don't push test results").append($("<br>"));
            }

            pipelineDiv.append("Tags: ").append($("<br>"));
            var tags = $("<div>");
            pipelineDiv.append(tags);
            pipeline.tags.forEach(addTag);

            if (firstTag()) {
                var add = $("<input type='button' value='Add'>");
                add.click(function () {
                    var first = firstTag();
                    addTag(first);
                    apply.push(function () {
                        pipeline.tags.push(first);
                    });
                    dirty.push(function () {
                        return true; // there is new tag
                    });
                });
                pipelineDiv.append(add);
            }
        }

        var CONFIRMATION = "There are unsaved changes, if you continue they will be discarded. Continue?";
        var pipelineSelect;

        if (jobConfiguration.pipelines.length == 0) {
            var createPipelineDiv = $("<div>No pipeline is currently defined for this job<br/></div>");
            var createPipelineButton = $("<input type='button' value='Create Pipeline'>");
            createPipelineButton.click(function () {
                pipelineDiv.empty();
                var pipeline = {
                    isRoot: true,
                    noPush: false,
                    tags: []
                };
                jobConfiguration.pipelines.push(pipeline);
                renderPipeline(pipeline);
                buttons.append(applyButton);
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
                    if (dirtyFields()) {
                        if (!window.confirm(CONFIRMATION)) {
                            lastSelected.attr("selected", true);
                            return;
                        }
                    }
                    lastSelected = $(pipelineSelect).find("option:selected");
                    renderPipeline(jobConfiguration.pipelines[pipelineSelect[0].selectedIndex]);
                });
                result.prepend(pipelineSelect);
                selectedIndex = pipelineSelect[0].selectedIndex;
            }
            renderPipeline(jobConfiguration.pipelines[selectedIndex]);
            buttons.append(applyButton);
        }

        var originalUnload = window.onbeforeunload;
        window.onbeforeunload = function() {
            if (dirtyFields()) {
                return CONFIRMATION;
            } else {
                // keep original check just in case there is another dirty data (shouldn't be)
                return originalUnload();
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

        function removeEmptyTags(pipeline) {
            for (var i = 0; i < pipeline.tags.length; i++) {
                if (pipeline.tags[i] == null) {
                    pipeline.tags.splice(i--, 1);
                }
            }
        }

        function saveConfiguration(pipeline) {
            if (!validateFields()) {
                return;
            }
            applyFields();
            removeEmptyTags(pipeline);

            status.empty();
            progressFunc("Storing configuration on server");
            proxy.storeJobConfigurationOnServer(jobConfiguration, function (t) {
                progressFunc();
                var response = t.responseObject();
                if (response.errors.length > 0) {
                    response.errors.forEach(function (error) {
                        var errorDiv = $("<div class='error'><font color='red'><b/></font></div>");
                        errorDiv.find("b").text(error);
                        status.append(errorDiv);
                    });
                } else {
                    renderConfiguration(response.config, pipeline.id);
                }
            });
        }
    }

    return {
        configure: configure
    };
}