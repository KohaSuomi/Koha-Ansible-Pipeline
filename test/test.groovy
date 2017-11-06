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
jenv.EXECUTOR_NUMBER = 2

def currentBuild = [:]
currentBuild.result = 'FAILURE'

def verbose = 1

/* GITHUB PARAMS */
def gitconnection = [:]
gitconnection.baseurl = "https://api.github.com/repos"
gitconnection.organization = "KohaSuomi"
gitconnection.repo = "Hetula"
gitconnection.branch = "master"



/* <!START TESTING!> */
ks.Util ks = new ks.Util(gitconnection, jenv, currentBuild, verbose)

/* GIT */
println "********************************"
println "*Newest git commit from Hetula:*"
println "********************************"

def gitcommit = ks.gh.getNewestCommit('master')

println gitcommit
println ""

if (gitcommit.author =~ /(?i)kivilahti/) {
  println gitcommit.author+" contains /(?i)kivilahti/";
}
else {
  println gitcommit.author+" doesn't contain /(?i)kivilahti/";
}
println ""

println "*************************************************************"
println "*Newest commit from a branch from Koha-translations:*"
println "*************************************************************"

gitconnection.repo = "Koha-translations"
ks.Util ks_translations = new ks.Util(gitconnection, jenv, currentBuild, verbose)

gitcommit = ks_translations.gh.getNewestCommit()

def inMaster =     ks_translations.gh.isCommitInBranch(gitcommit, 'master')
def inTesting =    ks_translations.gh.isCommitInBranch(gitcommit, 'testing')
def inProduction = ks_translations.gh.isCommitInBranch(gitcommit, 'production')

println "inMaster: $inMaster, inTesting: $inTesting, inProduction: $inProduction"
println ""

ks.setCommitToBuild( gitcommit, 'master' );

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

println "*************************************************"
println "*ks.emailToUsername('johanna.raisa@mikkeli.fi'):*"
println "*************************************************"
println ks.emailToUsername("johanna.raisa@mikkeli.fi")
assert ks.emailToUsername("johanna.raisa@mikkeli.fi") == 'jraisa'
println ""

