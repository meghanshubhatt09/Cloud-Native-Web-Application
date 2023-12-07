#!/bin/bash
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-cache search openjdk-17
echo "---x---Java Install---x---"
sudo apt install openjdk-17-jdk -y

echo "JAVA PATH"
# /usr/lib/jvm/java-17-openjdk-amd64/
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/" >>~/.bashrc
echo "export PATH=$PATH:$JAVA_HOME/bin" >>~/.bashrc

echo "Installing Maven"
sudo apt install maven -y



# UBUNTU CONNECTION
# ssh ubuntu@44.212.0.134 -i ~/.ssh/aws -v

# echo "Installing Application"
# scp -i /Users/meghanshubhatt/.ssh/aws /Users/meghanshubhatt/Desktop/ass8/webapp/target/webapp-0.0.1-SNAPSHOT.jar ubuntu@ec2-107-22-117-120.compute-1.amazonaws.com:~
# java -jar webapp-0.0.1-SNAPSHOT.jar
# echo "Starting Application"
# java -jar webapp-0.0.1-SNAPSHOT.jar
# /var/lib/cloud/scripts/per-boot/
sudo apt-get install htop

echo "Installing CloudWatch"
sudo curl -o /root/amazon-cloudwatch-agent.deb https://s3.amazonaws.com/amazoncloudwatch-agent/debian/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i -E /root/amazon-cloudwatch-agent.deb

echo "Configuring Cloudwatch Agent"
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/home/ubuntu/cloudwatch-config.json \
    -s
# scp -i /Users/meghanshubhatt/.ssh/aws /Users/meghanshubhatt/Desktop/ass5/webapp/target/webapp-0.0.1-SNAPSHOT.jar ubuntu@ec2-44-212-37-226.compute-1.amazonaws.com:~


### DEBUG TOMCAT 
# ps -ef | grep tomcat 

# #Reload tomcat service
# sudo systemctl daemon-reload

# #Restart/Start tomcat service
# sudo systemctl start tomcat
# sudo systemctl stop tomcat

# #Check tomcat service status
# systemctl status tomcat.service

# scp -i /Users/meghanshubhatt/.ssh/aws /Users/meghanshubhatt/Desktop/ass5/webapp/target/webapp-0.0.1-SNAPSHOT.jar ubuntu@ec2-44-212-37-226.compute-1.amazonaws.com:~
