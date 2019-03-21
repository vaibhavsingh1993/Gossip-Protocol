#!/bin/bash

#ADS Project deployment steps on CentOS

sudo yum -y install java-1.8.0-openjdk-devel
sudo yum -y install git
git clone https://github.com/vaibhavsingh1993/Gossip-Protocol
sudo yum -y install wget
wget https://services.gradle.org/distributions/gradle-5.3-bin.zip
sudo yum -y install unzip
unzip gradle-5.3-bin.zip

mkdir -p /opt/gradle
sudo mv gradle-5.3/ /opt/gradle
export PATH=$PATH:/opt/gradle/gradle-5.3/bin
