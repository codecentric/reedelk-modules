
function deployModule(formData) {
    $.ajax({
        url: Constants.ModuleDeployApiPath,
        data: formData,
        processData: false,
        contentType: false,
        type: 'POST',
        success: function (data) {
            console.log('successss');
            toastr.success('Module deployed');

            setTimeout(function() {
                $("#deployed-modules tbody tr").remove();
                listModules();
            }, 1000);

        },
        error: function (data) {
            toastr.error('Module could not be deployed');
        },
        complete: function () {
            console.log('complete');
        }
    });
}

function listModules() {
    $.get(Constants.ModuleApiPath, function (data) {
        Utilities.SortByModuleName(data.modules);
        ModulesTableRenderer.Render(data.modules);
    });
}

function updateModule(moduleName, moduleFilePath) {
    var body = JSON.stringify({ moduleFilePath: moduleFilePath});
    $.ajax({
        url: Constants.ModuleApiPath,
        type: 'PUT',
        contentType: "application/json",
        data: body,
        success: function(result) {
            toastr.success('Module "' + moduleName + '" updated');

            setTimeout(function() {
                $("#deployed-modules tbody tr").remove();
                listModules();
            }, 1000);

        },
        error: function(result) {
            toastr.error('Module "' + moduleName + '" could not be updated');
        }
    });
}

function removeModule(moduleName, moduleFilePath) {
    var body = JSON.stringify({ moduleFilePath: moduleFilePath});
    $.ajax({
        url: Constants.ModuleApiPath,
        type: 'DELETE',
        contentType: "application/json",
        data: body,
        success: function(result) {
            toastr.success('Module "' + moduleName + '" removed');

            setTimeout(function() {
                $("#deployed-modules tbody tr").remove();
                listModules();
            }, 1000);

        },
        error: function(result) {
            toastr.error('Module "' + moduleName + '" could not be removed');
        }
    });
}