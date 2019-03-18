function listModules() {
    $.get("/api/module", function (data) {
        $('#deployed-modules').bootstrapTable('load', data.modules);
    });
}

function updateModule(name, moduleFilePath) {
    $.post("/api/module", JSON.stringify({moduleFilePath: moduleFilePath}), function () {
        toastr.success('Successfully updated module "' + name + '"');
        setTimeout(listModules, 1000);
    }, "json").fail(function (error) {
        toastr.error('Could not updated module "' + name + '"');
        console.log(error);
    });
}

