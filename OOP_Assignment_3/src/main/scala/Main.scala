import java.util.Date

// region Headers
//iso_code,
//continent,
//location,
//date,
//total_cases,
//new_cases,
//new_cases_smoothed,
//total_deaths,
//new_deaths,
//new_deaths_smoothed,
//total_cases_per_million,
//new_cases_per_million,
//new_cases_smoothed_per_million,
//total_deaths_per_million,
//new_deaths_per_million,
//new_deaths_smoothed_per_million,
//reproduction_rate,
//icu_patients,
//icu_patients_per_million,
//hosp_patients,
//hosp_patients_per_million,
//weekly_icu_admissions,
//weekly_icu_admissions_per_million,
//weekly_hosp_admissions,
//weekly_hosp_admissions_per_million,
//new_tests,
//total_tests,
//total_tests_per_thousand,
//new_tests_per_thousand,
//new_tests_smoothed,
//new_tests_smoothed_per_thousand,
//positive_rate,
//tests_per_case,
//tests_units,
//total_vaccinations,
//people_vaccinated,
//people_fully_vaccinated,
//new_vaccinations,
//new_vaccinations_smoothed,
//total_vaccinations_per_hundred,
//people_vaccinated_per_hundred,
//people_fully_vaccinated_per_hundred,
//new_vaccinations_smoothed_per_million,
//stringency_index,
//population,
//population_density,
//median_age,
//aged_65_older,
//aged_70_older,
//gdp_per_capita,
//extreme_poverty,
//cardiovasc_death_rate,
//diabetes_prevalence,
//female_smokers,
//male_smokers,
//handwashing_facilities,
//hospital_beds_per_thousand,
//life_expectancy,
//human_development_index,
// endregion

object Main {
  def main(args: Array[String]): Unit = {
    val filePath = "resources/covid-data.csv"
    val csvTable = CSVFileReader.readCSVFromPath(filePath)

    // total deaths in Malaysia
    val malaysiaFilter = Filter("location", EqualOp_String("Malaysia"))
    val totalDeaths_Malaysia = csvTable.filter(malaysiaFilter).extractColumn("total_deaths").last.toDoubleOption match {
      case Some(x) => x
      case None => 0
    }
    println(s"Total deaths from Covid-19 in Malaysia : $totalDeaths_Malaysia")

    println("Please specify your country")
    val inputCountry = scala.io.StdIn.readLine().trim
    val locationFilter = Filter("location", EqualOp_String(inputCountry))
    val filteredTable = csvTable.filter(locationFilter)

    val totalDeathColumn = filteredTable.extractColumn("total_deaths")

    val totalDeaths = totalDeathColumn.last.toDoubleOption match {
      case Some(value) => value
      case None => 0
    }
    val avgDeathPerDay = totalDeaths / totalDeathColumn.length
    print(s"Total deaths in $inputCountry : $totalDeaths.\nAverage deaths per day : $avgDeathPerDay.")
//    val totalDeathColumn = filteredTable.extractColumn("")

  }
}

object CSVFileReader {
  def readCSVFromPath(path: String): CSVTable = {
    val bufferedSource = io.Source.fromFile(path)
    val linesFromFile = bufferedSource.getLines

    val table = new CSVTable(linesFromFile.toArray)
    bufferedSource.close

    table
  }
}

class CSVTable(val headers: Array[String], val rows: Array[Array[String]], val filters: Filter*) {
  private val headerLookup: Map[String, Int] = headers.zipWithIndex.map(x => {
    (x._1, x._2)
  }).toMap

  def this(data: Array[String]) {
    this(data.head.split(","), data.tail.map(x => x.split(",").map(x => x.trim)))
  }

  def extractColumn(header: String): Array[String] = extractColumn(headerLookup.apply(header))

  def extractColumn(columnIndex: Int): Array[String] = rows.map(_.apply(columnIndex))

  def filter(filters: Filter*): CSVTable = {
    def filterRow(row: Array[String]): Boolean = {
      for (filter <- filters) {
        val column = headerLookup.apply(filter.column)
        if (filter.operation.apply(row(column))) {
          return true
        }
      }
      false
    }

    new CSVTable(headers, rows.filter(filterRow))
  }
}

case class Filter(column: String, operation: FilterOperation)

trait FilterOperation {
  def apply(leftOperand: String): Boolean
}

case class EqualOp_String(rightOperand: String) extends FilterOperation {
  override def apply(leftOperand: String): Boolean = leftOperand == rightOperand
}

case class StartsWithOp(rightOperand: String) extends FilterOperation {
  override def apply(leftOperand: String): Boolean = {
    leftOperand.startsWith(rightOperand)
  }
}

case class BetweenDatesOp(startDate: Date, endDate: Date, format: String = "yyyy-mm-dd") extends FilterOperation {
  private val formatter = new java.text.SimpleDateFormat(format)

  override def apply(leftOperand: String): Boolean = {
    val date = formatter.parse(leftOperand)

    // todo need to check the equal conditions
    date.after(startDate) && date.before(endDate)
  }
}

case class EqualOp_Double(rightOperand: Double) extends FilterOperation {
  override def apply(leftOperand: String): Boolean = leftOperand.toDoubleOption match {
    case Some(x) =>
      x == rightOperand
    case _ =>
      false
  }
}

case class GreaterThanOp(rightOperand: Double) extends FilterOperation {
  override def apply(leftOperand: String): Boolean = leftOperand.toDoubleOption match {
    case Some(x) =>
      x > rightOperand
    case _ =>
      false
  }
}

case class GreaterThanEqualOp(rightOperand: Double) extends FilterOperation {
  override def apply(leftOperand: String): Boolean = leftOperand.toDoubleOption match {
    case Some(x) =>
      x >= rightOperand
    case _ =>
      false
  }
}

case class SmallerThanOp(rightOperand: Double) extends FilterOperation {
  override def apply(leftOperand: String): Boolean = leftOperand.toDoubleOption match {
    case Some(x) =>
      x < rightOperand
    case _ =>
      false
  }
}

case class SmallerThanEqualOp(rightOperand: Double) extends FilterOperation {
  override def apply(leftOperand: String): Boolean = leftOperand.toDoubleOption match {
    case Some(x) =>
      x <= rightOperand
    case _ =>
      false
  }
}
