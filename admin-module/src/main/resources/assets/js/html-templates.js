
const DetailsContainer = ({ content }) => `
    <div class="container-fluid module-details-container">
    	${content}
    </div>
`;

const DetailsRowItem = ({ title, content}) => `
    <div class="row">
        <div class="col-sm-1 module-details-row-item-title"><b>${title}</b></div>
        <div class="col-sm-11 module-details-row-item-content">${content}</div>
    </div>
`;