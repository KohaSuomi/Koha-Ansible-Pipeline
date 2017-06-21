#!groovy

import groovy.json.JsonSlurperClassic


/*
  DEFINING GLOBAL VARIABLES AND FUNCTIONS
*/

/* DEBUG PARAMS */

def env = System.getenv()
env['PIPE_DEBUG'] = 1


/* GITHUB PARAMS */
def githubBaseurl = "https://api.github.com/repos"
def organization  = "KohaSuomi"
def repo          = "Koha"
def branch        = "ks-rumble"

/* IRC MODIFIERS see. https://github.com/myano/jenni/wiki/IRC-String-Formatting*/
def IRCCLEAR = "\u000F"
def IRCRED   = "\u000304"
def IRCGREEN = "\u000303"
def IRCBROWN = "\u000305"
def IRCBOLD  = "\u0002"



def githubGetHEAD = {
    return _githubApiCall("$githubBaseurl/$organization/$repo/git/refs/heads/$branch")
}
def githubGetCommit = {
    hash ->
    return _githubApiCall("$githubBaseurl/$organization/$repo/git/commits/$hash")
}
def _githubApiCall(url) {
    def cmd = "/usr/bin/curl --silent $url"
    //Need to used StringBuilder here because the Process Streams get closed after they are read once.
    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = cmd.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(1000)

    // for debugging in Jenkins' Script Console
    println _dumpProcess(cmd, proc, sout, serr)
    if (proc.exitValue() != 0) {
        throw new Exception(_dumpProcess(cmd, proc, sout, serr))
    }
    return _parseJson(sout.toString())
}
def _parseJson(string) {
    return new groovy.json.JsonSlurperClassic().parseText(string)
}

def _sysCmd(cmd, timeout, verbose) {
    verbose = debug
    timeout = timeout ? timeout : 5000 //default timeout 5 seconds

    //Need to used StringBuilder here because the Process Streams get closed after they are read once.
    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = cmd.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(timeout)

    // for debugging in Jenkins' Script Console
    if (verbose && verbose == 1) {
        println _dumpProcess(cmd, proc, sout, serr, verbose)
    }
    if (proc.exitValue() != 0) {
        throw new Exception(_dumpProcess(cmd, proc, sout, serr))
    }
    return [proc, sout, serr]
}

def getSystemInfo {
    ok
    
}

def sendIrcMsg = {
    msg ->
    //Don't use "" in your message
    def cmd = "/usr/local/bin/sendIrcMsg.sh $msg"

    //Need to used StringBuilder here because the Process Streams get closed after they are read once.
    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = cmd.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(10000) //timeout here must be more than 5 seconds

    // for debugging in Jenkins' Script Console
    println _dumpProcess(cmd, proc, sout, serr)
    if (proc.exitValue() != 0) {
        throw new Exception(_dumpProcess(cmd, proc, sout, serr))
    }
}
def _dumpProcess(cmd, p, sout, serr, verbose) {
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
}

/*
  START BUILDING !!
*/

try {
    def githead    = githubGetHEAD()
    def gitcommit  = githubGetCommit(githead.object.sha)
    def gitmessage = gitcommit.message.tokenize("\n")[0]
    def gitauthor  = gitcommit.committer.name
} catch (e) {
    currentBuild.result = 'FAILURE'
    sendIrcMsg("Build "+env.BUILD_ID+"> ${IRCRED}Trying to build but GitHub API is malfunctioning?${IRCCLEAR}: "+e.toString())
    throw e
}
sendIrcMsg("Build "+env.BUILD_ID+"> ${IRCBROWN}${gitauthor} just committed '$gitmessage'. Starting build "+env.BUILD_URL+"${IRCCLEAR}")

/*
  Available environment variables, see. https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project
  invoke them with env.NODE_NAME
*/

scriptsDir='/opt/jenkins_ansbiletorpor_interface/'

stage('Provision') {
    def stageName = "Provision"
    node('master') {

        try {
            sendIrcMsg("Build "+env.BUILD_ID+"> ${IRCBROWN}Starting $stageName${IRCCLEAR}")

            sh "$scriptsDir/jenkins_interface.sh build koha_ci_1"

            sendIrcMsg("Build "+env.BUILD_ID+"> $stageName ${IRCGREEN}${IRCBOLD}PASSED${IRCCLEAR}")
        } catch(e) {
            currentBuild.result = 'FAILURE'
            sendIrcMsg("Build "+env.BUILD_ID+"> $stageName ${IRCRED}${IRCBOLD}FAILED${IRCCLEAR}: "+e.toString())
            throw e
        }
    }
}

stage('Git tests') {
    def stageName = "Git tests"
    node('master') {
        try {
            sendIrcMsg("${IRCBROWN}Build "+env.BUILD_ID+"> ${IRCBROWN}Starting $stageName${IRCCLEAR}")

            sh "$scriptsDir/jenkins_interface.sh git koha_ci_1"

            junit keepLongStdio: true, testResults: '**/testResults/junit/*.xml'

            step([
                $class: 'CloverPublisher',
                cloverReportDir: "testResults/clover",
                cloverReportFileName: 'clover.xml',
                healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80], // optional, default is: method=70, conditional=80, statement=80
                unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
                failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]     // optional, default is none
            ])

            sendIrcMsg("${IRCBROWN}Build "+env.BUILD_ID+"> $stageName ${IRCGREEN}${IRCBOLD}PASSED${IRCCLEAR}")

        } catch(e) {
            currentBuild.result = 'FAILURE'
            sendIrcMsg("${IRCBROWN}Build "+env.BUILD_ID+"> $stageName ${IRCRED}${IRCBOLD}FAILED${IRCCLEAR}: "+e.toString())
            throw e
        }
    }
}

stage('Full tests') {
    def stageName = "Full tests"
    node('master') {
        try {
            sendIrcMsg("${IRCBROWN}Build "+env.BUILD_ID+"> ${IRCBROWN}Starting $stageName${IRCCLEAR}")

            sh "$scriptsDir/jenkins_interface.sh all koha_ci_1"

            junit keepLongStdio: true, testResults: '**/testResults/junit/*.xml'

            step([
                $class: 'CloverPublisher',
                cloverReportDir: "testResults/clover",
                cloverReportFileName: 'clover.xml',
                healthyTarget: [methodCoverage: 70, conditionalCoverage: 80, statementCoverage: 80], // optional, default is: method=70, conditional=80, statement=80
                unhealthyTarget: [methodCoverage: 50, conditionalCoverage: 50, statementCoverage: 50], // optional, default is none
                failingTarget: [methodCoverage: 0, conditionalCoverage: 0, statementCoverage: 0]     // optional, default is none
            ])

            sendIrcMsg("${IRCBROWN}Build "+env.BUILD_ID+"> $stageName ${IRCGREEN}${IRCBOLD}PASSED${IRCCLEAR}")

        } catch(e) {
            currentBuild.result = 'FAILURE'
            sendIrcMsg("${IRCBROWN}Build "+env.BUILD_ID+"> $stageName ${IRCRED}${IRCBOLD}FAILED${IRCCLEAR}: "+e.toString())
            throw e
        }
    }
}

stage('deploy to acceptance koha_ci') {

    sh "$scriptsDir/jenkins_interface.sh deploy"

}
