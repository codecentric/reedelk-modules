var Utilities = {
	
	Capitalize: function(string) {
  		if (typeof string !== 'string') return ''
  		var lowercased = string.toLowerCase();
  		return lowercased.charAt(0).toUpperCase() + lowercased.slice(1);
  	},

  SortByModuleName: function(list) {
    	list.sort((a, b) => (a.name > b.name) ? 1 : (a.name === b.name) ? ((a.name > b.name) ? 1 : -1) : -1 );
	},

  SortByFlowTitle: function(flows) {
      flows.sort((a, b) => (a.title > b.title) ? 1 : (a.title === b.title) ? ((a.title > b.title) ? 1 : -1) : -1 );
  },

	IconByModuleStatus: function(moduleStatus) {
       if (moduleStatus === "INSTALLED" || moduleStatus === "STARTED") {
       		return '<i class="fas fa-lg fa-check-circle success-color"></i>';
        } else if (moduleStatus == 'UNRESOLVED') {
        	return '<i class="fas fa-lg fa-exclamation-circle warn-color"></i>';
        } else if (moduleStatus == 'ERROR') {
        	return '<i class="fas fa-lg fa-exclamation-circle error-color"></i>';
        } else {
            return  moduleStatus;
        }
	}
};