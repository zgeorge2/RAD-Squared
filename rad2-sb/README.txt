Starting the SB:

java -jar vapaas-sb-1.0-SNAPSHOT.jar server --akka.conf=application_akka_vap_service.conf --server.port=9080

Le-Mans server : 10.112.73.126
Here, in this setup the CSP is pointing to symphony-upgrade server. Certain commands may change
if you're using local CSP or CSP pointing to other setup

Getting the Access token -
curl -X POST   -H 'authorization: Basic bGUtbWFucy1nYXRld2F5Olo+SGordlk5Z2A0YUckeF4='   -H 'content-type: application/x-www-form-urlencoded'   -d 'grant_type=client_credentials'   http://csp-upgrade.symphony-dev.com/csp/gateway/am/api/auth/authorize
Set this value to Lemans-Token

Lemans-URI : Make sure the setup is pointing to lemans-resources

AgentId - Get this ID from the on-prem lemans-agent registered.

Requests -- 
Creds - admin/admin
Content-Type - application/json

1. Adding VAP instance
PATH - localhost:9080/vap-saas/infra/vap

Body - 
{
	"vapName":"testvap-2",
	"agentId" : "c3e51bb4-8e6a-454d-88ff-110216ad3e33"
}
agent id - lemans cleint id.

2. Configuring agent
PATH - localhost:9080/vap-saas/api/managedendpoints
Body - 
{
	"vapName":"testvap-2",
	"job":"install",
	"manageEndpointServiceStates": [
	{
		"vc_ip": "10.112.73.209",
		"endpoints": [{
			"vm_mor": "vm-512",
			"vc_id": "c3995515-5ba7-4055-a61c-6445415e1dc7",
			"user": "root",
			"password": "Password!"
		},{
			"vm_mor": "vm-516",
			"vc_id": "c3995515-5ba7-4055-a61c-6445415e1dc7",
			"user": "root",
			"password": "Password!"
		}]
	}
]
}

Zipkin - zipkin jar in bundled in code-base.
Start the server - java -jar zipkin.jar
Port:9411


