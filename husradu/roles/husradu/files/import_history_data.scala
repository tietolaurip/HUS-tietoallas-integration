import fi.tietoallas.integration.pseudonymization._
import fi.tietoallas.integration.pseudonymization.identity._
import com.google.gson._
import scala.collection.JavaConverters._

val ruleFile = "file:/home/jussi/Data/Husradu/husradu.json"
val sourcePattern = "file:/home/jussi/Data/Husradu/history/*"
val pseudoProxyAddress = "http://localhost:3000/pseudo"

val rules = new java.io.ByteArrayInputStream(sc.textFile(ruleFile).toLocalIterator.mkString.getBytes("UTF-8"))
val identityManager = new IdentityManagerRemoteImpl(pseudoProxyAddress, "datalake")
val ps = new HusraduPseudonymizer(identityManager, ruleStream)

val sourceObjects = sc
    .textFile(sourcePattern)
    .map(new JsonParser().parse(_).getAsJsonArray)
    .flatMap(_.iterator.asScala.toSeq)

