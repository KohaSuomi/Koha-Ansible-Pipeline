package ks

import groovy.json.JsonSlurperClassic
import ks.Util

/**

Global Groovy Shared library for Koha-Suomi Jenkins Pipeline jobs

with help from: https://www.slideshare.net/roidelapluie/jenkins-shared-libraries-workshop

**/

class GitHub implements Serializable {

  /* GITHUB PARAMS */
  String githubBaseurl = "https://api.github.com/repos"
  String organization  = "KohaSuomi"
  String repo          = "Hetula"
  String branch        = "master"
  ks.Util ks
  Integer verbose

  def githead, gitcommit

  GitHub(def gitconnection, ks.Util ks, Integer verbosity) {
    this.ks = ks
    this.verbose = verbosity
    this.githubBaseurl = gitconnection.baseurl
    this.organization  = gitconnection.organization
    this.repo          = gitconnection.repo
    this.branch        = gitconnection.branch
  }

  def _parseJson(string) {
    return new groovy.json.JsonSlurperClassic().parseText(string)
  }

  def _githubApiCall(String url) {
    def cmd = "/usr/bin/curl --silent $url"
    def rv = ks.sysCmd(cmd, 5000)
    def proc = rv[0]
    def sout = rv[1]
    def serr = rv[2]

    return _parseJson(sout.toString())
  }

  def githubGetHEAD() {
    return _githubApiCall("$githubBaseurl/$organization/$repo/git/refs/heads/$branch")
  }

  def githubGetCommit(String hash) {
    return _githubApiCall("$githubBaseurl/$organization/$repo/git/commits/$hash")
  }

  def getLatestCommit() {
    if (gitcommit) {
      return gitcommit
    }

    try {
      githead    = githubGetHEAD()
      gitcommit  = githubGetCommit(githead.object.sha)
      def gittitle = gitcommit.message.tokenize("\n")[0]
      def gitauthor  = gitcommit.committer.name
      gitcommit.author = gitauthor
      gitcommit.title = gittitle
    } catch (e) {
      ks.currentBuild.result = 'FAILURE'
      ks.sendIrcMsgGitHubMalfunction(e)
      throw e
    }

    return gitcommit
  }

  def getRepositoryEvents() {
    return _githubApiCall("$githubBaseurl/$organization/$repo/events")
  }

  def getNewestCommitSha() {
    def events = getRepositoryEvents()

    String commitSha;
    for (int i=0 ; i<events.size() ; i++) {
      commitSha = events[i]?.payload?.commits[0]?.sha
      if (commitSha) { return commitSha }
    }
    throw new Exception("No commit in the newest events list?")
  }

  def isCommitInBranch(String branchName) {
    def commitSha = getNewestCommitSha()
    def comparison = _githubApiCall("$githubBaseurl/$organization/$repo/compare/$branchName...$commitSha")
    if (comparison.status == 'identical' || comparison.status == 'behind') {
      return true
    }
    else {
      return false
    }
  }
}
