var ModulesTableRenderer = (function() {

	var render = function(modules) {
		var table = document.getElementById("deployed-modules");

		for (var index in modules) {
        	var module = modules[index];
        	var id = col(module.moduleId);
        	var name = col(module.name);
        	var version = col(module.version);
        	var collapsed = col('<i class="fas fa-lg fa-caret-right"></i>', "align-center");
        	var status = col(Utilities.IconByModuleStatus(module.state), 'align-center');
        	var path = col(module.moduleFilePath);
        	var allColumns = collapsed + status + id + name + version + path;
        
        	$(table).find('tbody').append(row(allColumns, module.moduleId));
        	$(table).find('tbody').append(hiddenRow(details(module), module.moduleId));
        }

        $('.hiddenRow').on('shown.bs.collapse', function (item) {
            var caret = $(item.currentTarget.parentElement.previousSibling.firstChild.childNodes[0]);
            caret.removeClass('fa-caret-right');
            caret.addClass('fa-caret-down');
        });

        $('.hiddenRow').on('hidden.bs.collapse', function (item) {
            var caret = $(item.currentTarget.parentElement.previousSibling.firstChild.childNodes[0]);
            caret.removeClass('fa-caret-down');
            caret.addClass('fa-caret-right');
        });
	};

	var details = function(module) {
		var detailsContent = '';
		var state = Utilities.Capitalize(module.state);
    	if (state) {
    	    detailsContent += [{ title: 'State:', content: state}].map(DetailsRowItem).join('');
    	}
    	var flows = module.flows;
    	if (flows) {
    		Utilities.SortByFlowTitle(flows);
    		var flowsContent = flows.map(function(flow) {
    			if (flow.title) {
    				return '<b>' + flow.title + '</b> (id: ' + flow.id + ')';
    			} else {
					return '<b>ID:</b> ' + flow.id;
    			}
    		}).join('<br>');
    		if (flowsContent) {
				detailsContent += [{ title: 'Flows:', content: flowsContent}].map(DetailsRowItem).join('');
    		}
    	}
    	var resolved = module.resolvedComponents.join('<br>');
    	if (resolved) {
    	    detailsContent += [{ title: 'Resolved components:', content: resolved}].map(DetailsRowItem).join('');
    	}
    	var unresolved = module.unresolvedComponents.join('<br>');
    	if (unresolved) {
    	    detailsContent += [{ title: 'Unresolved components:', content: unresolved}].map(DetailsRowItem).join('');
    	}
    	var errors = module.errors.map(function(error){ return error.replace(/\n/g, "<br/>")}).join('<hr>');
    	if (errors) {
    	    detailsContent += [{ title: 'Errors:', content: errors}].map(DetailsRowItem).join('');   
    	}
    	return [{ content: detailsContent }].map(DetailsContainer).join('');
	};

	var row = function(data, id) {
		return '<tr data-toggle="collapse" data-target="#' + id + '">' + data + '</tr>';
	};

	var hiddenRow = function(data, id) {
		return '<tr><td class="hiddenRow no_top_bottom_border" colspan="6"><div id="' + id + '" class="collapse" data-parent="#modulesTableContainer">' + data + '</div></td></tr>';	
	};

	var col = function(data, classes) {
		return classes != undefined ?
			'<td class="' + classes + '" >' +  data + '</td>':
			'<td>' +  data + '</td>';
	};

	return {
		Render: render
	}

})();