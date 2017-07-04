# Koha-Ansible-Pipeline

Contains modules to interface with underlying tools and programs that are needed
to do continuous delivery (CD) with our Koha+Ansible+AnsbileTorpor+Jenkins -toolchain.

See
https://tiketti.koha-suomi.fi:83/projects/smd/wiki/Koha-Suomi_services#Jenkins2-and-Continuous-Delivery-via-AnsbileTorpor

## Jenkins Build Pipeline for Koha

Jenkins Build Pipeline as code, Jenkins should load the build instructions from this directory

### Hetula

https://github.com/KohaSuomi/Hetula

/Jenkins-Pipeline/Hetula has the Jenkinsfile to build/test/deploy Hetula

## Koha-Suomi Test Harness

Harness the power of clover and junit in one easy to use wrapper.
Simply give a list of test files to execute and he will take care of all the configuration hassle to get Clover and Junit
working with Perl.

## Jenkins Groovy global shared directories

https://jenkins.io/doc/book/pipeline/shared-libraries/
https://www.slideshare.net/roidelapluie/jenkins-shared-libraries-workshop

Files in

    /src

Are loadable as shared libraries for all Jenkins pipelines to use.

