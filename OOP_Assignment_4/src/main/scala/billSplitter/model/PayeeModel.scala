package billSplitter.model

import billSplitter.util.Database
import scalafx.beans.property.StringProperty
import scalikejdbc.{DB, WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef}

import scala.util.Try

class PayeeModel
(
  val id: Int,
  private val firstName: String,
  private val lastName: String,
  private val notes: String
) extends Database with InspectorItem {
  def fullName : String = firstName + " " + lastName

  val firstNameProp = new StringProperty(firstName)
  val lastNameProp = new StringProperty(lastName)
  val notesProp = new StringProperty(notes)

  override def uniqueId: Int = id
  override def displayName: String = fullName

  def save() : Try[Int] = {
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
					insert into
					  payee (firstName, lastName, notes)
					values
						(${firstNameProp.value}, ${lastNameProp.value}, ${notesProp.value})
				 """.update.apply()
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
				  update
				    payee
				  set
				    firstName   = ${firstNameProp.value},
            lastName    = ${lastNameProp.value},
            notes       = ${notesProp.value}
          where
            id = $id
         """.update.apply()
      })
    }
  }

  def delete() : Try[Int] = {
    if (isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
				  delete from
				    payee
				  where
            id = $id
				""".update.apply()
      })
    } else
      throw new Exception("Payee does not exist in Database")
  }

  def isExist : Boolean = PayeeModel.getPayeeById(id) match {
    case Some(x) => true
    case None => false
  }
}

object PayeeModel extends Database{
  def apply
  (
    id : Int,
    firstName : String,
    lastName : String,
    notes : String,
  ) : PayeeModel = {
      new PayeeModel(id, firstName, lastName, notes)
  }

  def initializeTable() = {
    DB autoCommit { implicit session =>
      sql"""
			create table payee (
			  id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
			  firstName varchar(64),
			  lastName varchar(64),
			  notes varchar(512)
			)
			""".execute.apply()
    }
  }

  def dropTable() = {
    DB autoCommit { implicit session =>
      sql"""
			drop table payee
			""".execute.apply()
    }
  }

  def getAllPayees : List[PayeeModel] = {
    DB readOnly { implicit session =>
      sql"select * from payee".map(resultSetToModel).list.apply()
    }
  }

  def getPayeeById(id : Int) : Option[PayeeModel] = {
    DB readOnly { implicit session =>
      sql"select * from payee where id = $id".map(resultSetToModel).single.apply()
    }
  }

  private def resultSetToModel(rs : WrappedResultSet) : PayeeModel = {
    PayeeModel(
      rs.int("id"),
      rs.string("firstName"),
      rs.string("lastName"),
      rs.string("notes")
    )
  }
}
