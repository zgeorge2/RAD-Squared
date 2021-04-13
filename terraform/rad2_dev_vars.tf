#################
# AWS Variables #
#################
variable rad2_ami {
    # get ami image names and instance_types from AWS dashboard of your account
    description = "The AMI image to use for each RAD2 node in the cluster"
    type = string
    default = "ami-089fe7d1d6c16f83a" # specific to ap-southeast-1 (Singapore). Use your own.
}

variable rad2_instance_type {
    description = "The EC2 instance type to use for each RAD2 node in the cluster"
    type = string
    # need 2-4 vCPU and 8-16 GiB at least.
    default = "t2.large"
}

variable rad2_instance_volume_size {
    # at least 50 GiB volume size
    description = "The EC2 instance volume size to use"
    type = number
    default = 50
}

variable rad2_aws_profile {
    # The profile refers Terraform to the AWS credentials
    # stored in the named AWS config file,
    description = "The AWS profile to use when connecting to AWS account"
    type = string
    default = "default"
}

variable rad2_aws_region {
    description = "The AWS region to deploy the cluster"
    type = string
    default = "ap-southeast-1"   # Singapore
}

# unused. key-pairs are created by terraform. But, if you need to use your own
# specify the path to the public key pair here. Uncomment in main.tf. Comment out
# terraform based creation.
variable rad2_ssh_rsa_pub {
    description = "My RSA pub key from my ~/.ssh/id_rsa.pub file"
    type = string
    default = "~/.ssh/id_rsa.pub"
}

##########################
# RAD2 Cluster Variables #
##########################
variable "rad2_cluster_nodes" {
    description = "The list of RAD2 Nodes to create"
    type = list(string)
    default = ["RAD2-LAX", "RAD2-NYC", "RAD2-SEA"]
}

variable "rad2_cluster_ports" {
    description = "The list of RAD2 Ports that the RAD2 Nodes will listen on"
    type = list(number)
    default = [9080, 9090, 10080]
}

variable "rad2_cluster_jmx_remote_ports" {
    description = "The list of JMX Remote Ports for each RAD2 node"
    type = list(number)
    default = [9081, 9091, 10081]
}

variable "rad2_cluster_akka_configs" {
    description = "The list of RAD2 akka config files for RAD2 nodes"
    type = list(string)
    default = ["application_akka_LAX.conf", "application_akka_NYC.conf", "application_akka_SEA.conf"]
}

variable "rad2_jar_source" {
    description = "The RAD2 jar file source location in the dev env"
    type = string
    default = "../rad2-sb/target/rad2-sb-1.0-SNAPSHOT.jar"
}
variable "rad2_jar_destination" {
    description = "The RAD2 jar file destination on the RAD2_node"
    type = string
    default = "~/rad2-sb-1.0-SNAPSHOT.jar"
}

variable "aspectJ_jar_source" {
    description = "The AspectJWeaver jar file source location in the dev env"
    type = string
    default = "~/.rad2/lib/aspectjweaver.jar" # modify it to where ever it is in your dev env
}

variable "aspectJ_jar_destination" {
    description = "The AspectJWeaver jar file destination on the RAD2_node"
    type = string
    default = "~/aspectjweaver.jar"
}

variable "rad2_java_opts_mem" {
    description = "Memory settings for RAD2 java node"
    type = string
    default = "-Xms512m -Xmx4096m -XX:+HeapDumpOnOutOfMemoryError "
}

variable "rad2_java_opts_other" {
    description = "Other java opts for RAD21 java node"
    type = string
    default = "-Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 com.rad2.sb.app.SBApplication "
}

variable "rad2_spring_opts_profile" {
    description = "Spring profile to use"
    type = string
    default = "basic"  # choices are dev/basic - see rad2-sb/src/resources/application-[dev|basic].yaml
}

variable "rad2_spring_opts_other" {
    description = "Default spring options to use for java run"
    type = string
    default = "-Dspring.output.ansi.enabled=always -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true "
}
