from vmware_symphony_deploy import kubernetes_lib
import LemansRegistrar
import logging
import json
import requests
from requests.auth import HTTPBasicAuth


def createLemansRegistrar(kube_ns, cmdRunner, csp_uri, build_tag):
    '''
        1. get the lemans resources service name, lemans resources host name and mgmt plane host name
        2. get access token from CSP

    :param kube_ns:   kubernetes namespace
    :param cmdRunner: kubernetes command
    :param csp_uri:   csp uri
    :param build_tag: build number
    :return: LemansRegistrar object
    '''
    LEMANS_PORT = "8000"
    VAP_PORT = "9080"

    lemans_uri = kubernetes_lib.service_url(
        "lemans-resources-service", LEMANS_PORT, kube_ns, "").strip('/')
    logging.info("lemans uri - %s", lemans_uri)
    vap_saas_uri = kubernetes_lib.service_url(
        "vap-saas-sb-svc", VAP_PORT, kube_ns, "").strip('/')
    logging.info("vap-saas uri - %s", vap_saas_uri)
    logging.info("Getting csp access token for lemans")
    access_token = obtain_lemans_csp_token(csp_uri)

    return LemansRegistrar.LemansRegistrar(cmdRunner, access_token, lemans_uri, vap_saas_uri, kube_ns)


def obtain_lemans_csp_token(csp_uri):
    '''
    get csp token for lemans user

    :param csp_uri: csp uri
    :return:  csp token for lemans user
    '''
    client_id = 'le-mans-automation'
    client_secret = 'Z>Hj+vY9g`4aG$x^'

    cspOAuthUri = csp_uri + "/csp/gateway/am/api/auth/authorize"
    payload = 'grant_type=client_credentials'
    r = requests.post(cspOAuthUri, data=payload, auth=HTTPBasicAuth(
        client_id, client_secret), headers={'Content-Type': 'application/x-www-form-urlencoded'})
    data = json.loads(r.text)
    if "access_token" in data:
        logging.info("access token obtained from csp is %s",
                     data["access_token"])
    else:
        raise Exception(
            "ERROR: Get access token from CSP failed, output is %s", data)
    return data["access_token"]


def get_lemans_resources_service_name():
    '''
    get lemans resource service name

    :return: lemans resource service name
    '''
    selector = {"app": "lemans-resources"}
    try:
        services = kubernetes_lib.get_existing_services(selector)
        logging.info("Existing services - %s", services)
        for service_name in services:
            logging.info(
                "Service Name returned from kubernetes is %s", service_name)
            if "lemans-resources-service" in service_name:
                return service_name
    except:
        logging.exception(
            "Could not get the service name for lemans. Check if the deployment is up and running")
        return None

    return None
