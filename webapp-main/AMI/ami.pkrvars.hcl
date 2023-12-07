aws_region = "us-east-1"
source_ami = "ami-08c40ec9ead489470" # Ubuntu 22.04 LTS 
ssh_username = "ubuntu"
subnet_id = "subnet-0c3202697f19d8f93"
ami_regions = ["us-east-1",]
ami_user = ["292181623217","745959253811"]

#packer build --var-file=ami.pkrvars.hcl ami.pkr.hcl
#packer build ami.pkr.hcl 



