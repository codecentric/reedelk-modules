function listModules() {
    $.get("/module", function (data) {
        $('#deployed-modules').bootstrapTable({
            data: data.modules,
            onClickRow: function (row, $element) {
                updateModule(row.name, row.moduleFilePath);
            }
        });
    });
}

function updateModule(name, moduleFilePath) {
    $.post("/module", JSON.stringify({moduleFilePath: moduleFilePath}), function () {
        toastr.success('Successfully updated module "' + name + '"');
        listModules();
    }, "json").fail(function (error) {
        toastr.error('Could not updated module "' + name + '"');
        console.log(error);
    });
}

