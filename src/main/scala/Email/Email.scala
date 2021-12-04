package Email

object Email {

  def createEmail(logs: String) = {

    val email =
      s"""
         |Hello there.
         |
         |You might want to take a look at the LogFileGenerator application deployed on your AWS EKS Kubernetes cluster!
         |
         |LogFileGenerator produced the following errors/warnings:
         |
         |$logs
         |
         |""".stripMargin

    email

  }

}
