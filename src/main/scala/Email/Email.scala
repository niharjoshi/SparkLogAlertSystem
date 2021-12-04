package Email

object Email {

  // Function to create email body
  def createEmail(logs: String) = {

    // Creating email body
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

    // Returning email body
    email

  }

}
