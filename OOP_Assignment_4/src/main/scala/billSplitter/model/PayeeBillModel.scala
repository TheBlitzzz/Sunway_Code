package billSplitter.model

import billSplitter.model.PayeeBillModel.getPayeeBillByBothIds
import billSplitter.util.Database
import scalafx.beans.property.BooleanProperty
import scalikejdbc.{DB, WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef}

import scala.util.Try

class PayeeBillModel
(
  val payeeId: Int,
  val billId: Int,
  private val hasPaid: Boolean,
) extends Database {
  val hasPaidProp = BooleanProperty(hasPaid)

  def save() : Try[Int] = {
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
					insert into
					  payeeBill (payeeId, billId, hasPaid)
					values
						($payeeId, $billId, ${hasPaidProp.value})
				 """.update.apply()
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
				  update
				    payeeBill
				  set
				    hasPaid = ${hasPaidProp.value}
          where
            payeeId = $payeeId and
            billId = $billId
         """.update.apply()
      })
    }
  }

  def delete() : Try[Int] = {
    if (isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
				  delete from
				    payeeBill
				  where
            payeeId = $payeeId and
            billId = $billId
				""".update.apply()
      })
    } else
      throw new Exception("Payee does not exist in Database")
  }

  def isExist : Boolean = getPayeeBillByBothIds(payeeId, billId) match {
    case Some(x) => true
    case None => false
  }
}

object PayeeBillModel extends Database{
  def apply
  (
    payeeId : Int,
    billId : Int,
    hasPaid : Boolean,
  ) : PayeeBillModel = {
    new PayeeBillModel(payeeId, billId, hasPaid)
  }

  def initializeTable() = {
    DB autoCommit { implicit session =>
      sql"""
			create table payeeBill (
			  payeeId int,
			  billId int,
        hasPaid boolean
			)
			""".execute.apply()
    }
  }

  def dropTable() = {
    DB autoCommit { implicit session =>
      sql"""
			drop table payeeBill
			""".execute.apply()
    }
  }

  def getAllPayeeBills : List[PayeeBillModel] = {
    DB readOnly { implicit session =>
      sql"select * from payeeBill".map(resultSetToModel).list.apply()
    }
  }

  def getPayeeBillByBothIds(payeeId: Int, billId: Int) : Option[PayeeBillModel] = {
    DB readOnly { implicit session =>
      sql"select * from payeeBill where payeeId = $payeeId and billId = $billId".map(resultSetToModel).single.apply()
    }
  }

  def getPayeeBillByBillId(billId: Int) : List[PayeeBillModel] = {
    DB readOnly { implicit session =>
      sql"select * from payeeBill where billId = $billId".map(resultSetToModel).list.apply()
    }
  }

  private def resultSetToModel(rs : WrappedResultSet) : PayeeBillModel = {
    PayeeBillModel(
      rs.int("payeeId"),
      rs.int("billId"),
      rs.boolean("hasPaid"),
    )
  }
}
