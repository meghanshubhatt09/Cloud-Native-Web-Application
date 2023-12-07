variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "source_ami" {
  type    = string
  default = "ami-08c40ec9ead489470" # Ubuntu 22.04 LTS 
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = "subnet-0c3202697f19d8f93"
}

variable "ami_regions" {
  type    = list(string)
  default = [
    "us-east-1",
  ]
}

variable "ami_prefix" {
  type    = string
  default = "CSYE"
}

variable "ami_user" {
  type    = list(string)
  default = ["292181623217","745959253811"]
}

locals {
  timestamp = regex_replace(timestamp(), "[- TZ:]", "")
}



# https://www.packer.io/plugins/builders/amazon/ebs
source "amazon-ebs" "my-ami" {
  region          = "${var.aws_region}"
  ami_name        = "${var.ami_prefix}-${local.timestamp}"
  ami_description = "AMI for CSYE 6225"
  ami_regions = "${var.ami_regions}"
  ami_users = "${var.ami_user}"
  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  instance_type = "t2.micro"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = 8
    volume_type           = "gp2"
  }
}

build {
  sources = ["source.amazon-ebs.my-ami"]

  provisioner "file" {
    source      = "../target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/home/ubuntu/"
  }


  provisioner "file" {
    source      = "cloudwatch-config.json"
    destination = "/home/ubuntu/"
  }


provisioner "shell" {
  inline = [
    "sudo touch /home/ubuntu/application.properties",
    "sudo chmod 764 /home/ubuntu/application.properties",
  ]
}
  provisioner "shell" {
    scripts          = ["setup.sh"]
    environment_vars = [
      "DEBIAN_FRONTEND=noninteractive",
      "CHECKPOINT_DISABLE=1"
    ]
  }

provisioner "file" {
     source = "startup.sh"
     destination = "/home/ubuntu/"
   }

  provisioner "shell" {
       inline = [
        "sudo mv /home/ubuntu/startup.sh /var/lib/cloud/scripts/per-boot/",
         "sudo chmod 777 /var/lib/cloud/scripts/per-boot/startup.sh"]
  }
  # destination = "/var/lib/cloud/scripts/per-boot/"
  post-processor "manifest" {
        output = "manifestGetAMI.json"
        strip_path = true
    }
}




