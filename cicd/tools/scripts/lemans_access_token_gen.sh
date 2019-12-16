#! /bin/sh

lemans_resource_ip=$1
lemans_resource_port=$2
lemans_auth_token=$3
lemans_agent_name=vsphere-agent-zg$4

lemans_access_token=$(curl -X POST   http://$lemans_resource_ip:$lemans_resource_port/le-mans/v1/resources/access-keys -H 'content-type: application/json' -H "x-xenon-auth-token:$lemans_auth_token" -d "{\"name\": \"$lemans_agent_name\",\"orgId\": \"not-set\",\"createdBy\": \"test@vmware.com\"}")

echo $lemans_access_token
echo
