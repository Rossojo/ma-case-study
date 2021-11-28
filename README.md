# Master Thesis Multi-Deployment-Model Instance Model Retrieval and Instance Model Management - Case Study Setup Guide

This setup guide provides you with instructions to run the case study of the master thesis "Multi-Deployment-Model
Instance Model Retrieval and Instance Management".

The case study sets up an instance of the sock shop microservice demo using different Deployment Technologies. The
catalogue microservice is deployed onto an EC2 Instance running Ubuntu 18.04. The Instance is created using either AWS
CloudFormation or Terraform, while the components of the catalogue service are installed onto the instance using Puppet.
All other microservices that make up the sock shop are deployed onto a local Kubernetes Cluster using Docker Desktop.

Using the prototype introduced in the thesis, an instance model of the sock shop instance can be generated and used to
manage the sock shop instance over the OpenTOSCA ecosystem.

## Setup the Sockshop

### Setup the Puppet Primary Server

Setup a VM running Ubuntu 20.04 on any platform you like. As the Puppet agent will be running on an EC2 instance, ensure
that your secuirty settings allow the Puppet Primary Server to accept incoming connections from the Puppet Agent. Make
the VM available via DNS so that the Puppet Agent can later connect to the Primary Server.

Now, we can start to setup Puppet and PostgreSQL on the primary server. The following steps are copied from
the [TOSCin repo](https://github.com/UST-EDMM/edmm/blob/53a945e49ea9246edfc81d3b8a4c0cc4b7ee7e48/TOSCin/readme.md). To
do this, enter following command on the Puppet master:

```shell script
wget https://apt.puppetlabs.com/puppet6-release-bionic.deb
sudo dpkg -i puppet6-release-bionic.deb
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo apt update
sudo apt -y install postgresql puppetserver
```

Puppet is installed now! Now we are going to configure the Puppet master. To do this, make following changes to the
puppet.conf file:
`sudo nano /etc/puppetlabs/puppet/puppet.conf`.

```
[main]
certname    = puppet-primary.example.com
server      = puppet-primary.example.com
environment = production
runinterval = 1y
```

Now, we setup the certificate authority by running:

```sudo /opt/puppetlabs/bin/puppetserver ca setup```
Once this is finished, we can start the Puppet master with following two commands:

```shell script
sudo systemctl enable puppetserver
sudo systemctl start puppetserver
```

Then, we are able to install PuppetDB. First, run these commands:

```shell script
sudo /opt/puppetlabs/bin/puppet resource package puppetdb ensure=latest
sudo /opt/puppetlabs/bin/puppet resource package puppetdb-termini ensure=latest
```

Now, it is required to configure PuppetDB. To do this, run the following command to create the first config file that is
required:

```shell script
sudo nano /etc/puppetlabs/puppet/puppetdb.conf
```

and add the following content:

```puppet
[main]
server_urls = https://localhost:8081
```

Then, configure the puppetDB config file:

```sudo nano /etc/puppetlabs/puppetdb/conf.d/database.ini```

```puppet
[database]
# The database address, i.e. //HOST:PORT/DATABASE_NAME
subname = //localhost:5432/puppetdb
# Connect as a specific user
username = puppetdb
# Use a specific password
password = puppetdb
# How often (in minutes) to compact the database
# gc-interval = 60
```

Now, edit the `sudo nano /etc/puppetlabs/puppet/puppet.conf` file by appending the following lines to the `[server]`
section:

```puppet
dns_alt_names        = puppet,puppet-master.test.com
storeconfigs         = true
storeconfigs_backend = puppetdb
reports              = store,puppetdb
```

Further, create a `routes.yaml` file in the same directory (`sudo nano /etc/puppetlabs/puppet/routes.yaml`) with the
following content:

```yaml
---
master:
  facts:
    terminus: puppetdb
    cache: yaml
```

Now, run the following four commands to configure the PostgreSQL database to use it with PuppetDB. When prompted,
enter `puppetdb` as password.

```shell script
sudo -u postgres sh
createuser -DRSP puppetdb
createdb -E UTF8 -O puppetdb puppetdb
psql puppetdb -c 'create extension pg_trgm'
exit
```

Now, restart the database:

```shell script
sudo service postgresql restart
```

Finally, start the PuppetDB up by firing this command:

```shell script
sudo /opt/puppetlabs/bin/puppet resource service puppetdb ensure=running enable=true
```

As a last step, we need to restart the Puppet server on the Puppet Master. This can be done for example by following
commands:

```shell script
sudo kill -HUP `pgrep -f puppet-server`
sudo service puppetserver reload
```

As the Puppet Primary server is now operational, you can upload the configuration files
from [puppet-master-environment](puppet-master-environment) to the primary server. First replace the node selector in
the [Manifest](puppet-master-environment/manifests/site-aws.pp) with the cert name you later intend to specifiy for the
Puppet Agent Node. Then, simply copy all directories and files
from [puppet-master-environment](puppet-master-environment) to `/etc/puppetlabs/code/environments/production`.

### Setup the Puppet Agent on AWS

The catalogue microservice is deployed on an EC2 Instance in the AWS cloud. First create a EC2 security group that
allows incoming traffic for SSH (port 22) and HTTP-ALT (port 8080). Also create a key pair with a custom name.

As Puppet is used to configure the created instance, first prepare
the [./puppet-agent-setup/puppet-setup.yaml](./puppet-agent-setup/puppet-setup.yaml) file. The file contains cloud-init
directives and is specified as user data for the EC2 instance. It ensures that Puppet is installed on the EC2 instance
after launch and that a catalogue-user is created that is used to run the catalogue microservice. In the file replace
the example values for *conf.agent.server* and *conf.agent.certname* with valid values for your setup. The *
conf.agent.server* property MUST be a resolveable DNS name under which your Puppet Primary Server is available. The *
conf.agent.client* property MUST equal the node selector you used previously when setting up the environment in the
primary server. However, the EC2 instance does not need to be accessible via DNS. The prototype solely relies on IP
addresses.

You can either use Terraform or AWS CloudFormation to create the EC2 instance. For both technologies a configuration
file is provided: [Terraform file](./puppet-agent-setup/main.tf)
and [CloudFormation template](puppet-agent-setup/aws-cloudformation-template.json). In both files replace the security
group id and the key name with the id of the security group and the name of the key pair you created previously. When
using CloudFormation you must also replace the user data in the template with the base64 encoded content of
your [puppet-setup.yaml](puppet-agent-setup/puppet-setup.yaml). Create the instance by either running `terraform apply`
or by uploading the [CloudFormation template](puppet-agent-setup/aws-cloudformation-template.json) using the AWS
ManagementConsole or CLI.

After the EC2 instance is running, the puppet agent is automatically installed on it and should automatically connect to
the puppet primary server. If the automatic connection fails, log in to the instance using your previously created key
pair and execute the following command:

```shell script
sudo /opt/puppetlabs/bin/puppet agent --test
```

The catalogue microservice should now be running on the instance. Check its status by executing the follwing command:

```shell script
sudo systemctl status catalogue-app
```

### Setup the Kubernetes Deployment

Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop)
and [enable the included Kubernetes Cluster](https://docs.docker.com/desktop/kubernetes). Ensure that your resulting
kubeconfig file contains the content of the required certificates directly instead of linking to other files.

Deploy the other sock shop microservices by deploying
the [complete-demo.yaml file](sock-shop-deployment/complete-demo.yaml):

```shell
kubectl apply -f sock-shop-deployment/complete-demo.yaml
```

Note, that some security feature on the specified containers MUST be disabled in order for the update management
operation to function correctly.
