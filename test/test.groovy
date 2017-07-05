import ks.Util


/* CHECK ENV */
if (! System.getenv()['MODE'] == 'testing') {
  println "Environment variable MODE is not 'testing'"
  System.exit(2)
}

/* MOCK Jenkins environment */
def jenv = [:]
jenv.BUILD_ID = 1000
jenv.BUILD_URL = "http://example.com/build_url"

def currentBuild = [:]
currentBuild.result = 'FAILURE'

def verbose = 2

/* GITHUB PARAMS */
def gitconnection = [:]
gitconnection.baseurl = "https://api.github.com/repos"
gitconnection.organization = "KohaSuomi"
gitconnection.repo = "Hetula"
gitconnection.branch = "master"



/* <!START TESTING!> */
ks.Util ks = new ks.Util(gitconnection, jenv, currentBuild, verbose)

/* GIT */
println "********************"
println "*Newest git commit:*"
println "********************"

def gitcommit = ks.gh.getLatestCommit()

println gitcommit.title
println gitcommit.author
println ""

/* IRC */
println "****************"
println "*IRC notifiers:*"
println "****************"

ks.irc.sendIrcMsg("Jenkins Pipeline IRC test")

ks.irc.sendIrcMsgPipelineStageFailure("badStage", new Exception("badStage test exception"))

println "**************************************"
println "*ks.ansbileTorporInterfaceScriptPath:*"
println "**************************************"
println ks.ansbileTorporInterfaceScriptPath
println ""

