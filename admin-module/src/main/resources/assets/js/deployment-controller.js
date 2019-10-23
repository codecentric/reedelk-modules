
function updateModule(name, moduleFilePath) {
    $.post(MODULE_API_PATH, JSON.stringify({moduleFilePath: moduleFilePath}), function () {
        toastr.success('Successfully updated module "' + name + '"');
        setTimeout(listModules, 1000);
    }, "json").fail(function (error) {
        toastr.error('Could not update module "' + name + '"');
        console.log(error);
    });
}

function listModules() {
    $.get(Constants.ModuleApiPath, function (data) {
        Utilities.SortByModuleName(data.modules);
        ModulesTableRenderer.Render(data.modules);
    });
}