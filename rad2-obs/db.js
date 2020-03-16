/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

const request = require('request'); 
var body = '{"name": "Bank1","accountHolders": ["Charlie", "Chester"]}'

for(let i = 2; i < 1001 ; i++) {
	body = body + ',{"name": "Bank' + i + '","accountHolders": ["Charlie", "Chester"]}'
}

request.post({
		url : 'http://10.112.73.120:9080/vap/bank',
		headers : {
			'Authorization' : 'Basic dmFwc2Fhc2FkbWluQHZtd2FyZS5jb206Vk13YXJlQDEyMw',
			'Content-type' : 'application/json'
		},
		body : '{"banks": [' + body + ']}'
		
	}, (e, r, b) => {
		console.log(r.statusCode)
		console.log(e, b);
})