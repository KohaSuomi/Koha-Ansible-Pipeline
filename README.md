# Koha-Ansible-Pipeline
Jenkins Build Pipeline as code, Jenkins loads the build instructions from this repo

Jenkinsfile is the default name for the file Jenkins looks for for the build instructions.


1. Configure a Pipeline build job to get that build code from this SCM.
2. Test by triggering the build manually

3. Configure GitHub to do remote build execution on Git post-commit hook.
   https://help.github.com/articles/about-webhooks/

   Make this build listen for remote build triggers, using the "Trigger build remotely (e.g. from sripts)".
   Actually you put this remote listen url to the GitHub post-commit hook url target.
4. Test by pushing jiggly puff into your SCM

