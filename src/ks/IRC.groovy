package ks

import ks.Util

/**

Global Groovy Shared library for Koha-Suomi Jenkins Pipeline jobs

with help from: https://www.slideshare.net/roidelapluie/jenkins-shared-libraries-workshop

**/

class IRC implements Serializable {

  Integer verbose = 2

  public ks.Util ks

  String sendIrcMsgProgram = System.getenv()['MODE'] == 'testing' ? \
                             'test/mocks/sendIrcMsg.sh' : '/usr/local/bin/sendIrcMsg.sh'

  /* IRC MODIFIERS see. https://github.com/myano/jenni/wiki/IRC-String-Formatting*/
  def IRCCLEAR = "\u000F"
  def IRCRED   = "\u000304"
  def IRCGREEN = "\u000303"
  def IRCBROWN = "\u000305"
  def IRCBOLD  = "\u0002"


  /**

    @PARAM1 Jenkins environment -object
    @PARAM2 Jenkins current build -object
    @PARAM3 Integer, level of verbosity

  **/

  IRC(ks.Util ks, Integer verbosity) {
    this.verbose = verbosity
    this.ks = ks
  }

  /**

    Don't use "" in your message

  **/

  void sendIrcMsg(String msg) {
    def cmd = "$sendIrcMsgProgram $msg"
    def rv = ks.sysCmd(cmd, 10000)
    def proc = rv[0]
    def sout = rv[1]
    def serr = rv[2]
  }

  void sendIrcMsgPipelineStarted() {
    //if (verbose > 0) {
      def gitcommit = ks.gh.getLatestCommit()
      sendIrcMsg(ks.emailToUsername(gitcommit.committer.email)+": ${IRCBROWN}Build "+ks.env.BUILD_ID+"> ${gitcommit.author} just committed '${gitcommit.title}'. Starting pipeline "+ks.env.BUILD_URL+"${IRCCLEAR}")
    //}
  }

  void sendIrcMsgGitHubMalfunction(Exception e) {
    //if (verbose > 0) {
      def gitcommit = ks.gh.getLatestCommit()
      sendIrcMsg(ks.emailToUsername(gitcommit.committer.email)+": ${IRCBROWN}Build "+ks.env.BUILD_ID+"> ${IRCRED}Trying to build but GitHub API is malfunctioning?${IRCCLEAR}: "+e.toString())
    //}
  }

  void sendIrcMsgPipelineStageStarted(String stageName) {
    if (verbose > 0) {
      def gitcommit = ks.gh.getLatestCommit()
      sendIrcMsg(ks.emailToUsername(gitcommit.committer.email)+": ${IRCBROWN}Build "+ks.env.BUILD_ID+"> ${IRCBROWN}Starting $stageName${IRCCLEAR}")
    }
  }

  void sendIrcMsgPipelineStageSuccess(String stageName) {
    if (verbose > 0) {
      def gitcommit = ks.gh.getLatestCommit()
      sendIrcMsg(ks.emailToUsername(gitcommit.committer.email)+": ${IRCBROWN}Build "+ks.env.BUILD_ID+"> $stageName ${IRCGREEN}${IRCBOLD}"+(ks.currentBuild.result ?: "SUCCESS")+"${IRCCLEAR}")
    }
  }

  void sendIrcMsgPipelineStageFailure(String stageName, Exception e) {
    //if (verbose > 0) {
      def gitcommit = ks.gh.getLatestCommit()
      sendIrcMsg(ks.emailToUsername(gitcommit.committer.email)+": ${IRCBROWN}Build "+ks.env.BUILD_ID+"> $stageName ${IRCRED}${IRCBOLD}"+ks.currentBuild.result+"${IRCCLEAR}: "+e.toString())
    //}
  }

  void sendIrcMsgPipelineSuccess() {
    //if (verbose > 0) {
      def gitcommit = ks.gh.getLatestCommit()
      sendIrcMsg(ks.emailToUsername(gitcommit.committer.email)+": ${IRCBROWN}Build "+ks.env.BUILD_ID+"> Commit '${gitcommit.title}' ${IRCGREEN}passed the pipeline.${IRCCLEAR}")
    //}
  }

}
