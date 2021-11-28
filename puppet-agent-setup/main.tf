terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.50"
    }
  }

  required_version = ">= 1.0.0"
}

provider "aws" {
  region     = "eu-central-1"
  profile = "default"
}

data "template_file" "user_data" {
  template = file("./puppet-setup.yaml")
}

resource "aws_instance" "puppet-agent" {
  ami           = "ami-00105f70a3660f2ae"
  instance_type = "t2.micro"

  root_block_device {
    volume_size = 16
  }

  vpc_security_group_ids = [
  "sg-0be5360e140d4cf62"]

  key_name = "puppet-master-keys"
  
  user_data = data.template_file.user_data.rendered
}

output "public_ip" {
  value = aws_instance.puppet-agent.public_ip
}
