var ModuleDetailsRenderer = (function() {

	var render = function(module) {

		var detailsContent = '';

		var state = Utilities.Capitalize(module.state);
    	if (state) {
    	    detailsContent += Template.DetailsRowItem('State:', state);
    	}

    	var flows = module.flows;
    	if (flows) {
    		detailsContent += renderFlows(flows);
    	}

    	var resolved = module.resolvedComponents.join('<br>');
    	if (resolved) {
    	    detailsContent += Template.DetailsRowItem('Resolved components:', resolved);
    	}

    	var unresolved = module.unresolvedComponents.join('<br>');
    	if (unresolved) {
    	    detailsContent += Template.DetailsRowItem('Unresolved components:', unresolved);
    	}

    	// The JSON containing the exceptions string contains new lines. 
    	// We replace '\n' with the HTML '<br/>' whenever a new line is found.
    	var errors = module.errors.map(function(error){ return error.replace(/\n/g, "<br/>")}).join('<hr>');
    	if (errors) {
    	    detailsContent += Template.DetailsRowItem('Errors:', errors);
    	}

    	return Template.DetailsContainer(detailsContent);

	};

	var renderFlows = function(flows) {
    		Utilities.SortByFlowTitle(flows);
    		var flowsContent = flows.map(function(flow) {
    			return flow.title ?
    				'<b>' + flow.title + '</b> (id: ' + flow.id + ')':
    				'<b>ID:</b> ' + flow.id;
    		}).join('<br>');
		return flowsContent ? Template.DetailsRowItem('Flows:', flowsContent) : '';
	};

	return {
		Render: render
	}

})();