#!groovy

/**

IN THIS FILE:

Mocks a build environment for Jenkins2 Pipeline scripts so one can test that
they actually compile before pushing to SCM

Compiles and executes the Jenkins2 Pipeline script
Jenkinsfile

See.
https://stackoverflow.com/questions/7421993/how-to-list-all-binding-variables-with-groovyshell#7422385

**/


/*
  SET THE JENKINS ENVIRONMENT

  Prepare variables for injection into the Pipeline script
*/

Binding bindings = [
    /* https://support.cloudbees.com/hc/en-us/articles/218554077-How-to-set-current-build-result-in-Pipeline */
    'currentBuild': [:],
    /*
      Available environment variables, see. https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project
      invoke them with env.NODE_NAME
    */
    'env': ['BUILD_ID': 666,
            'BUILD_URL': 'https://jenkins.example.org',
            'EXECUTOR_NUMBER': 1
           ]
]

/*
  COMPILE SCRIPT AND INJECT ENVIRONMENT
*/

GroovyShell gs = new GroovyShell()
Script script = gs.parse(new File('Jenkinsfile')) //Compile the script, and make it ready to run
script.setBinding(bindings)

/*
  RUN AND PRAY
*/

script.run()

