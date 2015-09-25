# Overview

This is a version of the
[Floodlight](www.projectfloodlight.org/floodlight/) controller with
support for OpenFlow 1.3. The original code from Big Switch was forked
off and updated. This repository is a work in progress.

The current code base is functional for the available applications /
modules like topology visualization, virtual networking, static flow
pushers, firewall, and loadbalancer.

# Release notes

* **Dependency on JOpenFlow**: This code depends on the
[JOpenFlow](http://bitbucket.org/sdnhub/jopenflow) repository also
maintained by SDN Hub. However, there are slight differences with
OFError and OFExperimenter. All "Experimenter" keyword is replaced with
"Vendor" in this repository.

* **Only works with OpenFlow 1.3 switches**: As of now, the
Floodlight-plus controller does not work with OF1.0 and OF1.3 switches
at the same time.

* **Support for some features still under works**: Although JOpenFlow
suppotrs multiple tables, meters, groups and IPv6, the Floodlight
implementation does not use them to its advantage. 

* **Unit tests still under development**: The unit tests in the src/tests
directory are still under development and will be updated soon.

# Maintainers
This code base is maintained by [SDN Hub](http://sdnhub.org). The author
is Srini Seetharaman (srini.seetharaman@gmail.com)

# Support/discussion forum

(http://sdnhub.org/forums/forum/controller-platforms/floodlight-plus/)

