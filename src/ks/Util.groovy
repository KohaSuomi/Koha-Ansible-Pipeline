package ks

import ks.GitHub

/**

Global Groovy Shared library for Koha-Suomi Jenkins Pipeline jobs

with help from: https://www.slideshare.net/roidelapluie/jenkins-shared-libraries-workshop

**/

class Util implements Serializable {

  //if this is defined, only projects whose full name matches this, can include this library
  String seedFullName = ""
  Integer verbose = 2

  public ks.GitHub gh
  public ks.IRC irc
  public def env
  public def currentBuild

  public String ansbileTorporInterfaceScriptPath = '/opt/Koha-Ansible-Pipeline/Jenkins-Pipeline/jenkinsToAnsbileTorporInterface.sh'

  /**

    @PARAM1 Jenkins environment -object
    @PARAM2 Jenkins current build -object
    @PARAM3 Integer, level of verbosity

  **/

  Util(def gitconnection, def env, def currentBuild, Integer verbosity) {
    this.env = env
    this.currentBuild = currentBuild
    this.verbose = verbosity
    this.gh = new ks.GitHub(gitconnection, this, this.verbose)
    this.irc = new ks.IRC(this, this.verbose)
  }

  public void setGitHubApi(ks.GitHub gh) {
    this.gh = gh
  }

  /**

    Don't use "" in your message

  **/

/**

  Executes a shell command

  WARNING!

  Using this pattern fails in the Jenkins Groovy sandbox:

    def (proc, sout, serr) = sysCmd(cmd, 5000)

  Use this instead:

    def rv = _sysCmd(cmd, 5000)
    def proc = rv[0]
    def sout = rv[1]
    def serr = rv[2]

**/

  def sysCmd(String cmd) {
    return sysCmd(cmd, 5000); //default timeout 5 seconds
  }

  def sysCmd(String cmd, Integer timeout) {
    //Need to use StringBuilder here because the Process Streams get closed after they are read once.
    StringBuilder sout = new StringBuilder(), serr = new StringBuilder()
    def proc = cmd.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(timeout)

    // for debugging in Jenkins' Script Console
    if (proc.exitValue() != 0) {
        throw new Exception(dumpProcess(cmd, proc, sout, serr, 2))
    }
    if (verbose > 1) {
        println dumpProcess(cmd, proc, sout, serr, verbose)
    }
    return [proc, sout, serr]
  }

  def dumpProcess(cmd, p, sout, serr, verbose) {
    def s = '';
    if (verbose && verbose == 2) {
        s += "COMMAND: "+cmd+"\n"
        s += "EXIT:    "+p.exitValue()+"\n"
        s += "STDERR:  "+serr+"\n"
        s += "STDOUT:  "+sout+"\n"
        return s
    }
    else if (verbose  &&  verbose == 1  &&  ( p.exitValue() != 0 || !serr.toString().isEmpty() )) {
        s += "COMMAND: "+cmd+"\n"
        s += "EXIT:    "+p.exitValue()+"\n"
        s += "STDERR:  "+serr+"\n"
        return s
    }
    return s
  }

  String emailToUsername(String email) {
    switch(email) {
      case "olli-antti.kivilahti@jns.fi":
        return "kivilahtio"
        break
      case "lari.taskula@jns.fi":
        return "lari"
        break
      case "johanna.raisa@mikkeli.fi":
        return "jraisa"
        break
      case "johanna.raisa@gmail.com":
        return "jraisa"
        break
      case "pasi.korkalo@oulu.fi":
        return "janPasi"
        break
    }
  }
}
