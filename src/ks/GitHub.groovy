package ks

import groovy.json.JsonSlurperClassic
import ks.Util
import ks.Git.Commit

/**

Global Groovy Shared library for Koha-Suomi Jenkins Pipeline jobs

with help from: https://www.slideshare.net/roidelapluie/jenkins-shared-libraries-workshop

**/

class GitHub implements Serializable {

  /* GITHUB PARAMS */
  String githubBaseurl = "https://api.github.com/repos"
  String organization  = "KohaSuomi"
  String repo          = "Hetula"
  ks.Util ks
  Integer verbose

  /**

  @param Map gitconnection Git connection defaults and parameters
                  .baseurl String github api url basically
                  .organization String github organization, eg. KohaSuomi
                  .repo String the repository to target, eg. Koha-translations
                  .defaultBranch String default branch for API operations

  */
  GitHub(def gitconnection, ks.Util ks, Integer verbosity) {
    this.ks = ks
    this.verbose = verbosity
    this.githubBaseurl    = gitconnection.baseurl
    this.organization     = gitconnection.organization
    this.repo             = gitconnection.repo
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

    if (serr) {
      ks.currentBuild.result = 'FAILURE'
      ks.irc.sendIrcMsgGitHubMalfunction(e)
      throw e
    }

    return _parseJson(sout.toString())
  }

  def getHEADCommitSha(String branchName) {
    def ref = _githubApiCall("$githubBaseurl/$organization/$repo/git/refs/heads/$branchName")
    if (ref?.object?.type == 'commit') {
      return ref.object.sha
    }
    else {
      throw new Exception("GitHub HEAD ref is not a commit? Ref dump:\n$ref")
    }
  }

  def getCommit(String sha) {
    String url = "$githubBaseurl/$organization/$repo/git/commits/$sha";
    def commit = _githubApiCall(url)
    if (! commit?.sha) {
      throw new Exception("No commit found from url '$url'")
    }
    return new ks.Git.Commit(commit)
  }

  def getRepositoryEvents() {
    String url = "$githubBaseurl/$organization/$repo/events";
    def events = _githubApiCall(url)
    if (! events?.type) {
      throw new Exception("No events found from url '$url'")
    }
    return events
  }

  def getNewestCommit(String branchName) {
    String sha = getHEADCommitSha(branchName)
    return getCommit(sha)
  }

  def getNewestCommit() {
    def events = getRepositoryEvents()

    for (int i=0 ; i<events.size() ; i++) {
      def commit = events[i]?.payload?.commits[0]
      if (commit) {
        return new ks.Git.Commit(commit)
      }
    }
    throw new Exception("No commit in the newest events list? Events dump:\n$events")
  }

  /**
   * GitHub API has no way of clearly telling in which branch a commit is, so we must guess
   */
  def isCommitInBranch(def commit, String branchName) {
    if (! commit?.sha) {
      throw new Exception("Commit is missing 'sha'? Commit dump:\n$commit");
    }
    def comparison = _githubApiCall("$githubBaseurl/$organization/$repo/compare/$branchName..."+commit.sha)
    if (comparison.status == 'identical' || comparison.status == 'behind') {
      return true
    }
    else {
      return false
    }
  }
}

