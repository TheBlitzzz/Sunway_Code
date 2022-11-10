package billSplitter.model

import billSplitter.model.BillModel.getBillById
import billSplitter.util.Database
import org.joda.time.DateTime
import scalafx.beans.property.{DoubleProperty, LongProperty, StringProperty}
import scalikejdbc.{DB, WrappedResultSet, scalikejdbcSQLInterpolationImplicitDef}

import java.time.LocalDate
import scala.util.Try

class BillModel
(
  val id: Int,
  private val title: String,
  private val description: String,
  private val billDate: Long,
  private val total : Double,
  private val taxRate : Double,
  private val tipRate : Double,
) extends InspectorItem
{
  val titleProp = new StringProperty(title)
  val descriptionProp = new StringProperty(description)
  val dateProp = LongProperty(billDate)
  val totalProp = DoubleProperty(total)
  val taxRateProp = DoubleProperty(taxRate)
  val tipRateProp = DoubleProperty(tipRate)

  def calculatedTotalAfterTaxAndTips: Double = {
    totalProp.value * (1 + (taxRateProp.value / 100) + (tipRateProp.value / 100))
  }

  def billLocalDate_=(localDate: LocalDate): Unit = {
    dateProp.value = new DateTime(localDate.getYear, localDate.getMonthValue, localDate.getDayOfMonth, 0, 0).getMillis
  }
  def billLocalDate : LocalDate = {
    val billDate = new DateTime(dateProp.value)
    LocalDate.of(billDate.getYear, billDate.getMonthOfYear, billDate.getDayOfMonth)
  }

  override def uniqueId: Int = id
  override def displayName: String = titleProp.value

  def save() : Try[Int] = {
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
					insert into
					  bill (title, description, billDate, total, taxRate, tipRate)
					values
						(${titleProp.value}, ${descriptionProp.value}, ${dateProp.value}, ${totalProp.value}, ${taxRateProp.value}, ${tipRateProp.value})
				 """.update.apply()
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
				  update
				    bill
				  set
				    title       = ${titleProp.value},
            description = ${descriptionProp.value},
            billDate    = ${dateProp.value}
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
				    bill
				  where
            id = $id
				""".update.apply()
      })
    } else
      throw new Exception("Bill does not exist in Database")
  }

  def isExist : Boolean = getBillById(id) match {
    case Some(x) => true
    case None => false
  }
}

object BillModel extends Database{
  def apply
  (
    id: Int,
    title: String,
    description: String,
    billDate: Long,
    total : Double,
    taxRate : Double,
    tipRate : Double,
  ) : BillModel = {
    new BillModel(id, title, description, billDate, total, taxRate, tipRate)
  }

  def initializeTable(): Boolean = {
    DB autoCommit { implicit session =>
      sql"""
			create table bill (
			  id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
			  title varchar(64),
			  description varchar(512),
			  billDate bigint,
        total double,
        taxRate double,
        tipRate double
			)
			""".execute.apply()
    }
  }

  def dropTable() = {
    DB autoCommit { implicit session =>
      sql"""
			drop table bill
			""".execute.apply()
    }
  }

  def getAllBills : List[BillModel] = {
    DB readOnly { implicit session =>
      sql"select * from bill".map(resultSetToModel).list.apply()
    }
  }

  def getBillById(id : Int) : Option[BillModel] = {
    DB readOnly { implicit session =>
      sql"select * from bill where id = $id".map(resultSetToModel).single.apply()
    }
  }

  private def resultSetToModel(rs : WrappedResultSet) : BillModel = {
    BillModel(
      rs.int("id"),
      rs.string("title"),
      rs.string("description"),
      rs.long("billDate"),
      rs.double("total"),
      rs.double("taxRate"),
      rs.double("tipRate"),
    )
  }
}
