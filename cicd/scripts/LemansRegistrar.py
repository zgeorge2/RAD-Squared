from string import Template
import logging
import json

CHECK_DEPLOYMENT_INTERVAL = 10
CURL_BASE = "curl --max-time " + str(CHECK_DEPLOYMENT_INTERVAL) + " --silent "
RECEIVER_NAME_SUFFIX = "-receiver"
STREAM_NAME_SUFFIX = "-stream"


class LemansRegistrar:
    def __init__(self, cmd_runner, accessToken, lemans_uri, vap_saas_uri, namespace):
        self.namespace = namespace
        self.cmd_runner = cmd_runner
        self.accessToken = accessToken
        self.lemans_uri = lemans_uri
        self.vap_saas_uri = vap_saas_uri

    def get_resource(self, resource_type, resource_name):
        curl_cmd = Template(
            CURL_BASE +
            "-H x-xenon-auth-token:" + self.accessToken + " " +
            "-X GET " +
            "${lemans_uri}/le-mans/v2/resources/${resource_type}/${resource_name}").substitute(
                lemans_uri=self.lemans_uri,
                resource_type=resource_type,
                resource_name=resource_name)

        return self.cmd_runner.execute(curl_cmd, self.namespace)

    def delete_resource(self, resource_type, resource_name):
        curl_cmd = Template(
            CURL_BASE +
            "-H x-xenon-auth-token:" + self.accessToken + " " +
            "-X DELETE " +
            "${lemans_uri}/le-mans/v2/resources/${resource_type}/${resource_name}").substitute(
                lemans_uri=self.lemans_uri,
                resource_type=resource_type,
                resource_name=resource_name)

        return self.cmd_runner.execute(curl_cmd, self.namespace)

    def get_stream(self, name):
        return self.get_resource("streams", name)

    # There is no delete_stream method because streams cannot be deleted.

    def stream_exists(self, name):
        response = self.get_stream(name)
        return "link" in response

    def create_stream(self, name, receiver_link):
        curl_cmd = Template(
            CURL_BASE +
            "-H x-xenon-auth-token:" + self.accessToken + " " +
            "-H Content-Type:application/json -X POST " +
            "${lemans_uri}/le-mans/v2/resources/streams " +
            "--data {" +
            "\"name\":\"${name}\"," +
            "\"deliveryPolicy\":\"WAIT_ALL\"," +
            "\"receiverLinks\":[\"${receiver_link}\"]}").substitute(
                lemans_uri=self.lemans_uri,
                name=name,
                receiver_link=receiver_link)

        return self.cmd_runner.execute(curl_cmd, self.namespace)

    def delete_receiver(self, name):
        self.delete_resource("receivers", name)

    def get_receiver(self, name):
        return self.get_resource("receivers", name)

    def receiver_exists(self, name):
        receiver = self.get_receiver(name)
        return "link" in receiver

    def update_receiver(self, name, address):
        curl_cmd = Template(
            CURL_BASE +
            "-H x-xenon-auth-token:" + self.accessToken + " " +
            "-H Content-Type:application/json -X PATCH " +
            "${lemans_uri}/le-mans/v2/resources/receivers/${name}" +
            " --data {" +
            "\"address\":\"${address}\"}").substitute(
                lemans_uri=self.lemans_uri,
                name=name,
            address=address)

        return self.cmd_runner.execute(curl_cmd, self.namespace)

    def create_receiver(self, name, address):
        curl_cmd = Template(
            CURL_BASE +
            "-H x-xenon-auth-token:" + self.accessToken + " " +
            "-H Content-Type:application/json -X POST " +
            "${lemans_uri}/le-mans/v2/resources/receivers " +
            "--data {" +
            "\"name\":\"${name}\"," +
            "\"address\":\"${address}\"," +
            "\"useHttp2\":true," +
            "\"operationTimeoutMicros\":30000000}").substitute(
                lemans_uri=self.lemans_uri,
                name=name,
                address=address)

        return self.cmd_runner.execute(curl_cmd, self.namespace)

    def register(self, streams_and_receivers_map):
        for stream, receiver in streams_and_receivers_map.items():
            logging.info("Registering receiver " +
                         receiver + " with stream " + stream)
            self.register_stream_and_receiver(stream, receiver)

    def register_stream_and_receiver(self, stream, receiver_uri):
        """ For now, there's a one to one mapping between
        a stream and receiver, can be changed later"""
        stream_name = stream + STREAM_NAME_SUFFIX
        receiver_name = stream + RECEIVER_NAME_SUFFIX
        receiver_uri = receiver_uri if receiver_uri.startswith(
            '/') else '/' + receiver_uri
        address = self.vap_saas_uri + receiver_uri

        if self.receiver_exists(receiver_name):
            self.update_receiver(receiver_name, address)
        else:
            self.create_receiver(receiver_name, address)
            self.create_stream(
                stream_name, "/le-mans/v2/resources/receivers/" + receiver_name)
