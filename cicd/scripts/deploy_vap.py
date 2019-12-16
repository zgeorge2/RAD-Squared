#!/usr/bin/python -u
# Standard Libs
from collections import OrderedDict
from collections import namedtuple
from distutils.util import strtobool


from vmware_symphony_deploy import constants

from vmware_symphony_deploy.deployments.operations.abstract_service import (
    AbstractService
)
from vmware_symphony_deploy.deployments.dependency.serviceprovider.csp import CSP
from vmware_symphony_deploy.deployments.dependency.logforwarder.liagent import (
    LogInsight
)

from vmware_symphony_deploy.deployments.dependency.containerorchestration.kubernetes import (
    Kubernetes
)
from vmware_symphony_deploy.deployments.operations.factories.operation_meta_factory import (
    OperationMetaFactory
)
from vmware_symphony_deploy.deployments.operations.factories.operator_meta_factory import (
    OperatorMetaFactory
)
import logging
import LemansRegistrarFactory
import KubeCurl


VRBC_APP_INFO = OrderedDict()
VRBC_APP_INFO = {
    "svchost": {"name": "vap-saas-sb-svc",
                "gatewayregsvcname": "vap-saas-sb-svc",
                "ports": {"http": "9080"},
                },
    "labels": {"app": "vap-saas-sb"},
    "resources": {"app_cpu_requests": "1200m",
                  "app_mem_requests": "5000Mi",
                  "app_cpu_limits": "1200m",
                  "app_mem_limits": "5000Mi",
                  "app_jvm_mem": "4500",
                  },
    "replicas": {"replicas": 3,
                 "dev_replicas": 1,
                 "pods": 3,
                 "dev_pods": 1,
                 },
    "artifactory": {"internal": constants.INTERNAL_IMAGE_REPOSITORY,
                    "external": constants.EXTERNAL_IMAGE_REPOSITORY,
                    },
    "yamls": {
        "resource_yaml_deployment": "vap-saas-sb.yaml",
        "service_yaml": "vap-saas-sb-svc.yaml",
    },
    "application_strategy": "generic-rolling-upgrade",
    "application_properties": {
        "lemansauthid": "test-svc-1",
        "lemansauthsecret": "Z>Hj+vY9g`4aG$x^"
    },
    "operation_strategy": "kubernetes",
    "upgrade-strategy": "RollingUpdate",
    "external_dependencies": OrderedDict(
        [
            ("ContainerOrchestrator", {
                "dependency": False,
                "vendor": Kubernetes,
            }),
            ("Logging", {
                "vendor": LogInsight,
            }),
            ("PlatformServiceProvider", {
                "vendor": CSP,
                "role": "subscriber",
                "service_parameters": {
                    "redirect_uri": "",
                    "client_ids": "vapservice",
                    "csp_client_id": "vapservice",
                    "csp_service_auth_secret": "vapservice"
                }
            })
        ]),
    "gateway": {
        "vendor": "Heimdall",
        "gateway_regs_strategy": "deployment-scripts",
        "private_public_endpoints": [{"id": "vap_saas_auth",
                                      "public_path": "/vap/api/",
                                      "private_path": "/"
                                      }]
    }
}


class DeployVrbcService(AbstractService):

    ApplicationSpecificConfig = namedtuple(
        "ApplicationSpecificConfig",
        ["lemans_resources_url",
         "register_with_lemans"])

    class ApplicationSpecificConfig(ApplicationSpecificConfig):
        def __new__(
                cls,
                lemans_resources_url=None,
                register_with_lemans=False):
            return super().__new__(
                cls,
                lemans_resources_url=lemans_resources_url,
                register_with_lemans=register_with_lemans)

    def __init__(self):
        super(DeployVrbcService, self).__init__()
        self.appArgParser.add_argument("--lemans-resources-url",
                                       help="The lemans resources url",
                                       default=None)
        self.appArgParser.add_argument('--register-with-lemans',
                                       default=False,
                                       help='Register with Lemans')

    def init_config(self, args):
        application_properties = self.get_application_properties()
        lemans_default = "http://lemans-resources-service.{}.svc.cluster.local:8000".format(
            args.namespace)
        _lemans_resources_url = args.lemans_resources_url if args.lemans_resources_url else lemans_default
        _register_with_lemans = args.register_with_lemans

        self.specificConfig = self.ApplicationSpecificConfig(
            lemans_resources_url=_lemans_resources_url,
            register_with_lemans=_register_with_lemans)

        application_properties["lemansurl"] = _lemans_resources_url
        application_properties["registerWithLemans"] = _register_with_lemans

    def process_yaml(self, contents):
        resource_yaml = contents \
            .replace("$$LEMANS_RESOURCES_URL", self.specificConfig.lemans_resources_url)
        return resource_yaml

    def get_lemans_streams_receivers_map(self):
        """Map that contains streams and receivers as entries of
        Format - {"stream" : "relative receiver path"}
        Streams and receivers will be created if not exists.
        Stream name will be suffixed with -stream.
        Receiver name will be suffixed with -receiver.
        Keep this updated when new streams and receivers are added.
        """
        STREAMS_AND_RECEIVERS = {
            "vap-process": "/vap-saas/callback/process",
            "vap_control_plane_action": "/vap-saas/callback/controlplaneactions",
            "vap_endpoint_state": "/vap-saas/callback/endpointstate"
        }
        return STREAMS_AND_RECEIVERS

    def register_streams_and_receivers_with_lemans(self, streams_and_receivers_map, args):
        registrar = LemansRegistrarFactory.createLemansRegistrar(args.namespace,
                                                                 KubeCurl.KubeCurl(),
                                                                 args.csp_uri,
                                                                 args.tag)
        if registrar is not None:
            registrar.register(streams_and_receivers_map)

    def post_validation(self, **kwargs):
        logging.info("Performing post validation tasks.....")
        args = self.appArgParser.parse_args()
        should_register = bool(strtobool(str(args.register_with_lemans)))

        if should_register:
            logging.info("Begin Lemans streams and receivers registration")
            self.register_streams_and_receivers_with_lemans(
                self.get_lemans_streams_receivers_map(), args)
        else:
            logging.info("Skipping lemans registration")


if __name__ == "__main__":
    service = DeployVrbcService()
    service.deploy(
        app_info=VRBC_APP_INFO,
        service_obj=service,
        operation_meta_factory=OperationMetaFactory,
        operator_meta_factory=OperatorMetaFactory,
    )
