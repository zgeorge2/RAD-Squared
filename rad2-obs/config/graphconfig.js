module.exports = {
	nodes : {
		color : {
			actor : "rgba(66, 244, 98, 1)",
			system : "rgba(189, 2, 252, 1)",
			transient : "rgba(66, 134, 244, 1)",
			router: "rgba(180, 180, 0, 1)",
			routee: "rgba(180, 0, 0, 1)"
		}
	},
	links : {
		"parent-child" : {
			color : {
				opacity : 0.6,
				inherit : false
			}
		},
		local : {
			color : {
				color : "rgba(255, 76, 0, 1)"
			}
		},
		remote : {
			color : {
				color : "rgba(18, 69, 46, 0.7)"
			}
		},
		"dead-letter" : {
			color : {
				color : "rgba(0, 0, 0, 0.7)"
			}
		},
		maxWidth : 20
	}
}