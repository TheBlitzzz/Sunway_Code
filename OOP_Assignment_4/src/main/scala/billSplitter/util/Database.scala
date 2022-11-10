package billSplitter.util

import billSplitter.model.{PayeeModel, BillModel, PayeeBillModel}
import scalikejdbc._
trait Database {
  // initialize JDBC driver & connection pool
  // What's going on over here :
  // These are all Java functions
  // Class.forName initialises a class (the input string) and returns the Class<T> object
  // While initialising, it calls the class's static initializer
  // However, this only works with Java classes since there are no static classes in Scala.
  // Initialization code in a Scala companion object is only called when it is referenced (Like when calling one of its functions)
  private val derbyDriverClassname = classOf[org.apache.derby.jdbc.EmbeddedDriver].getName
  Class.forName(derbyDriverClassname)

  private val dbURL = "jdbc:derby:myDB;create=true;";
  ConnectionPool.singleton(dbURL, "me", "mine")

  // ad-hoc session provider on the REPL
  implicit val session = AutoSession


}
object Database extends Database{
  def setupDB() = {
    if (!hasPayeeDBInitialize){
      PayeeModel.initializeTable()
    }
    if (!hasBillDBInitialize) {
      BillModel.initializeTable()
    }
    if (!hasPayeeBillDBInitialize) {
      PayeeBillModel.initializeTable()
    }
  }

  private def hasPayeeDBInitialize : Boolean = {
    DB getTable "payee" match {
      case Some(x) => true
      case None => false
    }
  }

  private def hasBillDBInitialize : Boolean = {
    DB getTable "bill" match {
      case Some(x) => true
      case None => false
    }
  }

  private def hasPayeeBillDBInitialize : Boolean = {
    DB getTable "payeeBill" match {
      case Some(x) => true
      case None => false
    }
  }

  def resetDB(): Unit = {
    if (hasPayeeDBInitialize){
      PayeeModel.dropTable()
      PayeeModel.initializeTable()
    }
    if (hasBillDBInitialize) {
      BillModel.dropTable()
      BillModel.initializeTable()
    }
    if (hasPayeeBillDBInitialize) {
      PayeeBillModel.dropTable()
      PayeeBillModel.initializeTable()
    }
  }
}
