TAG ?= latest
REGISTRY = symphony-docker-local.jfrog.io/vap-saas
# Username and pwd will be rotated and must be fed externally as arg or env
USERNAME ?= $(JFROG_USER)
PASSWORD ?= $(JFROG_PASS)
REGISTRY_INTERNAL = symphony-docker-local.artifactory.eng.vmware.com/vap-saas
USERNAME_INTERNAL = "symphony-deployer"
PASSWORD_INTERNAL = "Symphony1!"
IMAGE = $(REGISTRY)/vap-saas-sb
IMAGE_INTERNAL = $(REGISTRY_INTERNAL)/vap-saas-sb
DOCKER_BUILD_DIR = .
PROJECT_DIR = ../..
VAP_SAAS_SB_JAR = vap-saas-sb.jar

build: prep
	docker login --username $(USERNAME) --password $(PASSWORD) $(REGISTRY)
	docker login --username $(USERNAME_INTERNAL) --password $(PASSWORD_INTERNAL) $(REGISTRY_INTERNAL)
	docker build --no-cache=true -t $(IMAGE):$(TAG) $(DOCKER_BUILD_DIR) -f $(DOCKER_BUILD_DIR)/base/Dockerfile
	docker tag $(IMAGE):$(TAG) $(IMAGE):latest
	docker tag $(IMAGE):$(TAG) $(IMAGE_INTERNAL):$(TAG)

prep: clean
	@mkdir -p $(DOCKER_BUILD_DIR)/base/lib
	@cp -r $(PROJECT_DIR)/vapaas-sb/target/vapaas-sb-*-SNAPSHOT.jar $(DOCKER_BUILD_DIR)/base/lib/$(VAP_SAAS_SB_JAR)

clean:
	-rm -r $(DOCKER_BUILD_DIR)/base/lib/$(VAP_SAAS_SB_JAR)

push:
	docker push $(IMAGE):$(TAG)
	docker push $(IMAGE):latest
	docker push $(IMAGE_INTERNAL):$(TAG)

shell:
	@docker run --rm -ti -v `pwd`:/src --entrypoint=/bin/sh $(IMAGE)
