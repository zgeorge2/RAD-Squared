# ################################
# PROVIDER SPEC
# ################################
# https://www.terraform.io/docs/language/providers/requirements.html
terraform {
    required_providers {
        aws = {
            source = "registry.terraform.io/hashicorp/aws"
            # use maximum provider version
            version = "~> 3.27"
        }
    }
}

# configure the provider: AWS
# pre-req: Configure AWS CLI, so the profile is available
provider "aws" {
    profile = var.rad2_aws_profile
    region = var.rad2_aws_region
}

# ################################
# SECURITY GROUP SPEC
# ################################
resource "aws_security_group" "rad2_node_sg" {
    name = "rad2_node_sg"
    description = "Used to setup SSH and HTTP access for each RAD2 node"
    ingress {
        description = "SSH access from anywhere"
        from_port = 22
        to_port = 22
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
        ipv6_cidr_blocks = ["::/0"]
    }

    ingress {
        description = "http access from anywhere"
        from_port = 80
        to_port = 80
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
        ipv6_cidr_blocks = ["::/0"]
    }

    ingress {
        description = "https/TLS access from anywhere"
        from_port = 443
        to_port = 443
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
        ipv6_cidr_blocks = ["::/0"]
    }

    ingress {
        description = "ICMP access from anywhere"
        from_port = 8
        to_port = 0
        protocol = "icmp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    ingress {
        description = "ICMP access from anywhere"
        from_port = 8
        to_port = 0
        protocol = "icmpv6"
        ipv6_cidr_blocks = ["::/0"]
    }

    egress {
        description = "HTTP access to the internet"
        from_port = 80
        to_port = 80
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
        ipv6_cidr_blocks = ["::/0"]
    }

    egress {
        description = "outbound internet access"
        from_port = 0
        to_port = 0
        protocol = "-1"
        cidr_blocks = ["0.0.0.0/0"]
        ipv6_cidr_blocks = ["::/0"]
    }
}

# ################################
# CREATE KEY-PAIR
# ################################
# specify the key pair to use with each rad2 node from local file system
#resource "aws_key_pair" "rad2_node_keypair" {
#    key_name = "rad2_node_keypair"
#    public_key = var.rad2_ssh_rsa_pub
#}

# Create a key pair with the given key_name
resource "tls_private_key" "this" {
    algorithm = "RSA"
}

module "key_pair" {
    source = "terraform-aws-modules/key-pair/aws"
    key_name = "rad2_node_keypair"
    public_key = tls_private_key.this.public_key_openssh
}

# If you need to extract the pem file from the heredoc string
# in this output variable, use the following
# $ cat<<EOT | sed  '1d;$d' | tee xx > /dev/null
# > $(tf output tls_private_key_pem)
# > EOT
# sed will remove the first and last lines containing >>EOT and EOT, the limiters
# tee will place the output from sed into the file xx
# and the redirection to /dev/null will ensure the output doesn't go to stdout
# $(tf ...) runs the terraform out put on this output variable
# the final EOT ends the input to cat
#output "tls_private_key_pem" {
#    sensitive = true
#    description = "the private key PEM created by module key_pair"
#    value = tls_private_key.this.private_key_pem
#}

# Writing the pem file to .ssh dir in home directory
# this is only for use with ssh on the local command line
# refer to the instances "connect" button for the SSH command to use
# it will need this pem file. Typically the command would look like¬
# $ ssh -i "<this pem file>" ubuntu@<hostname of EC2 instance>
resource "local_file" "rad2_node_keypair_pem" {
    content = tls_private_key.this.private_key_pem
    filename = "./rad2_node_keypair.pem"
    file_permission = "0400"
}

# ################################
# AWS Instance(s) Specification
# ################################
resource "aws_instance" "RAD2_Cluster" {
    count = length(var.rad2_cluster_nodes)
    ami = var.rad2_ami
    instance_type = var.rad2_instance_type
    vpc_security_group_ids = [aws_security_group.rad2_node_sg.id]
    key_name = "rad2_node_keypair" # use the key_name of the aws_key_pair resource specified earlier
    root_block_device {
        volume_size = var.rad2_instance_volume_size
    }

    tags = {
        Name = element(var.rad2_cluster_nodes, count.index)
    }

    # Connect to the instance
    connection {
        # The default username for our AMI
        user = "ubuntu"
        type = "ssh"
        host = self.public_ip
        private_key = tls_private_key.this.private_key_pem
        # The connection will use the local SSH agent for authentication.
    }

    provisioner "remote-exec" {
        inline = [
            "sudo apt-get -y update",
            "sudo apt-get -y install tree",
            "ls"
        ]
    }

    provisioner "remote-exec" {
        inline = [
            "sudo apt-get -y install openjdk-11-jdk-headless"
        ]
    }

    # upload RAD2 jar file
    provisioner "file" {
        source = var.rad2_jar_source
        destination = var.rad2_jar_destination
    }

    # upload AspectJWeaver jar file
    provisioner "file" {
        source = var.aspectJ_jar_source
        destination = var.aspectJ_jar_destination
    }

    # download logFile results
    provisioner "remote-exec" {
        inline = [
            templatefile("rad2_run.tpl", {
                rad2_jar_destination = var.rad2_jar_destination,
                rad2_java_opts_mem = var.rad2_java_opts_mem,
                rad2_java_opts_other = var.rad2_java_opts_other,
                aspectJ_jar_destination = var.aspectJ_jar_destination,
                jmxremote_port = element(var.rad2_cluster_jmx_remote_ports, count.index),
                rad2_spring_opts_profile = var.rad2_spring_opts_profile,
                rad2_spring_opts_other=var.rad2_spring_opts_other,
                server_port = element(var.rad2_cluster_ports, count.index),
                akka_conf = element(var.rad2_cluster_akka_configs, count.index)
            }),
            "ps -ef | grep java"
        ]
    }
}
