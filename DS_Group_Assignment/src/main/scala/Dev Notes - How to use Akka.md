# Notes from students

This md file is intended for our references as students during our studies and developing this project.
This file will mainly focus on the Akka system.

## The Akka system
Here are the interesting docs from Akka we read to get started:

### Receptionist
https://doc.akka.io/docs/akka/current/typed/actor-discovery.html

Link : Finding the actor refs

### Default config
We can also find the list of configurations here
-> Took a while for us to find what is akka.provider

Link : https://doc.akka.io/docs/akka/current/general/configuration-reference.html

### Clustering
For connecting to clients on other devices in the network
Key takeaways 
-> Each cluster is a group of actor systems that can communicate with each other
-> Each cluster has ONE leader node
-> Seed notes determine the contact points of the cluster (can have multiple contact points). It's just for nodes to join the cluster
-> Use cluster.manager ! Join(Address) to join a cluster

Link : https://doc.akka.io/docs/akka/current/typed/cluster.html

### Cluster Bootstrapping
To automatically connect without seed-nodes
But our service needs to be resolved by a DNS and IDK how to do that

#### Links
https://doc.akka.io/docs/akka-management/current/bootstrap/details.html
https://doc.akka.io/docs/akka-management/current/kubernetes-deployment/forming-a-cluster.html
https://doc.akka.io/docs/akka/current/discovery/index.html#discovery-method-dns

### Serialization
To serialize the actor messages

Link : https://doc.akka.io/docs/akka/current/serialization.html

## TODO for us
Understand the concept of watching another actor : https://doc.akka.io/docs/akka/current/typed/actor-lifecycle.html#watching-actors
