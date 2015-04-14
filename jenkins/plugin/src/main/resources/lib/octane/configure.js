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

    function renderConfiguration(jobConfiguration) {
        var result = $(target);
        result.empty();

        var pipelines = $("<div>");
        result.append(pipelines);

        var tagTypes = {};
        jobConfiguration.availableTags.forEach(function (tagType) {
            tagTypes[tagType.tagTypeId] = tagType;
        });

        var validators = [];

        function createPipeline(pipeline, pipelines) {

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

                function updateTag() {
                    tag.tagId = tagSelect.val();
                    tag.tagName = $(tagSelect).find("option:selected").text();
                }

                function updateTagType() {
                    loadTagValues();
                    tag.tagTypeId = tagTypeSelect.val();
                    tag.tagTypeName = $(tagTypeSelect).find("option:selected").text();
                    updateTag();
                }

                var container = $("<div>");
                tags.append(container);

                tagTypeSelect = $("<select>");
                jobConfiguration.availableTags.forEach(function (tagType) {
                    tagTypeSelect.append(new Option(tagType.tagTypeName, tagType.tagTypeId));
                });
                tagTypeSelect.val(tag.tagTypeId);
                tagTypeSelect.change(updateTagType);
                container.append(tagTypeSelect);

                tagSelect = $("<select>");
                loadTagValues();
                tagSelect.val(tag.tagId);
                tagSelect.change(updateTag);
                container.append(tagSelect);

                var remove = $("<input type='button' value='Remove'>");
                remove.click(function () {
                    var index = container.index();
                    pipeline.tags.splice(index, 1);
                    container.remove();
                });
                container.append(remove);
                container.append($("<br>"));
            }

            var pipelineDiv = $("<div>");
            pipelines.append(pipelineDiv);

            pipelineDiv.append("Pipeline: ");
            if (pipeline.isRoot) {
                var input = $("<input type='text' placeholder='Pipeline name'>");
                input.attr("value", pipeline.name);
                input.change(function () {
                    pipeline.name = input.val();
                });
                pipelineDiv.append(input);
                var validationArea = $("<div class='validation-error-area'>");
                var validate = function() {
                    if (!pipeline.name) {
                        validationArea.html("<div class='error'>Pipeline name must be specified</div>");
                        return false;
                    } else {
                        validationArea.empty();
                        return true;
                    }
                };
                input.blur(validate);
                validators.push(validate);
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
                select.change(function () {
                    pipeline.releaseId = select.val();
                });
                pipelineDiv.append(select).append($("<br>"));
            } else {
                var noPush = $("<input type='checkbox'>");
                noPush.change(function () {
                    pipeline.noPush = noPush.val();
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
                    pipeline.tags.push(first);
                });
                pipelineDiv.append(add);
            }
        }

        if (jobConfiguration.pipelines.length > 0) {
            jobConfiguration.pipelines.forEach(function (pipeline) {
                createPipeline(pipeline, pipelines);
            });
            addButtons();

        } else {
            var createPipelineDiv = $("<div>No pipeline is currently defined for this job<br/></div>");
            var createPipelineButton = $("<input type='button' value='Create Pipeline'>");
            createPipelineButton.click(function () {
                createPipelineDiv.remove();
                var pipeline = {
                    isRoot: true,
                    noPush: false,
                    tags: []
                };
                jobConfiguration.pipelines.push(pipeline);
                createPipeline(pipeline, pipelines);
                addButtons();
            });
            createPipelineDiv.append(createPipelineButton);
            pipelines.append(createPipelineDiv);
        }

        function addButtons() {

            function validateFields() {
                var valid = true;
                validators.forEach(function (validator) {
                    if (!validator()) {
                        valid = false;
                    }
                });
                return valid;
            }

            function saveConfiguration(close) {
                if (!validateFields()) {
                    return;
                }
                status.empty();
                progressFunc("Storing configuration on server");
                proxy.storeJobConfigurationOnServer(jobConfiguration, function (t) {
                    progressFunc();
                    var response = t.responseObject();
                    if (response.errors.length > 0) {
                        response.errors.forEach(function (error) {
                            var font = $("<font color='red'>");
                            font.text(error);
                            status.append(font)
                        });
                    } else if (close) {
                        // TODO: janotav: go to job page
                    } else {
                        renderConfiguration(response.config);
                    }
                });
            }

            var buttons = $("<div>");
            pipelines.append(buttons);

            var status = $("<div>");
            buttons.append(status);

            var saveButton = $("<input type='button' value='Save'>");
            saveButton.click(function () {
                saveConfiguration(true);
            });
            var applyButton = $("<input type='button' value='Apply'>");
            applyButton.click(function () {
                saveConfiguration(false);
            });
            buttons.append(saveButton).append(applyButton);
        }
    }

    return {
        configure: configure
    };
}