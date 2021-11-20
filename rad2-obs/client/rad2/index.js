var axios = require('axios');

const config = require("../../config/masterconfig").rad2;

async function updateRoutees(payload) {
    var data = JSON.stringify(payload);
      
    var requestDetails = {
        method: `post`,
        url: `http://${config.url}/adm/updateRoutees`,
        headers: { 
            'Content-Type': 'application/json'
        },
        data : data,
        auth: {
            username: `${config.username}`,
            password: `${config.password}`
        }
    };

    try {
        let response = await axios(requestDetails);
        console.log(JSON.stringify(response.data));
    } catch(e) {
        throw e;
    }
}

module.exports = {
    updateRoutees: updateRoutees
}