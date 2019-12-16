import logging
import sys
from vmware_symphony_deploy import kubernetes_lib
from vmware_symphony_deploy import infra_lib


class KubeCurl:

    def execute(self, curl_command, namespace):
        curl_output = kubernetes_lib.run_kubectl_busybox_cmd(
            curl_command, timeout=60, namespace=namespace)
        curl_output_json = infra_lib.find_and_parse_json(curl_output)
        logging.debug("output is %s", curl_output_json)
        return curl_output_json
