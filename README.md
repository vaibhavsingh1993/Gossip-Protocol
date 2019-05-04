<p align="center">
  <img width="200" height="200" src="https://upload.wikimedia.org/wikipedia/commons/e/e1/North_Carolina_State_University_Athletic_logo.svg">
</p>

# Gossip Protocol
CSC724 Spring 2019

## Authors
[Vaibhav Singh](https://github.ncsu.edu/vsingh7)(vsingh7@ncsu.edu) <br>
[Varun Madathil](https://github.ncsu.edu/vrmadath)(vrmadath@ncsu.edu) <br>
[Wayne Chen](https://github.ncsu.edu/cchen31)(cchen31@ncsu.edu) <br>

# Prerequisites

1. Install Git
2. Clone the repo
```
git clone https://github.com/vaibhavsingh1993/Gossip-Protocol.git
```
3. Install JDK (8+)
4. Install Gradle

It is highly recommended to run the code within VM's, preferably on the same Availability Zone (to reduce latency) with firewall rules allowing port **6991** to be able to send and receive UDP traffic. 

*Note: There are autoconfigure scripts named setup.sh and setup-ubuntu.sh which can be used to setup everything, but only if one is using CentOS / RHEL (for setup.sh) or Ubuntu (for setup-ubuntu.sh)*

```
chmod +x setup.sh <br>

./setup.sh <br>

(or) <br>

chmod +x setup-ubuntu.sh <br>

./setup-ubuntu.sh <br>

```

5. Once all dependencies are set up, run gradle
```
gradle run
```

6. (Optional) to change the behaviour of the nodes, you can edit the *src/config.properties* file to add or remove nodes, or to mark some of them as malicious or honest.

For example, to mark node 7 as malicious, set up the following configuration in all nodes:
```
vmname=gossip1 # **Change this to whichever node you are setting the config for.**
seed=1,0,0,0,1,1,1,0,0,1
adversaries=false,false,false,false,false,false,true,false,false,false # **Mark gossip7 as malicious**
nodelist=35.236.229.113,35.245.51.164,35.232.59.140,35.245.197.58,35.230.171.17,35.236.248.199,35.245.215.147,35.222.93.8,35.226.235.222,35.192.191.106
```
## Task distribution
TODO: Add it, probably as a wikipage?
</details>

## Screencast link
https://youtu.be/rtTEz17JtWU
</details>

### Setup used for Screencast
[PreSonus AudioBox USB 2-channel interface](https://www.amazon.com/PreSonus-AudioBox-USB-Audio-Interface/dp/B00154KSA2) <br>
[Shure SM58 microphone](https://www.shure.com/en-US/products/microphones/sm58) <br>
[Open Broadcasting Software OBS](https://obsproject.com/) <br>
[Avidemux](http://avidemux.sourceforge.net/) <br>


## Wiki and Repository Links

## Project Wiki
https://github.com/vaibhavsingh1993/Gossip-Protocol/wiki

## Forked Repositories
https://github.com/Tyler-R/Gossip-Protocol
