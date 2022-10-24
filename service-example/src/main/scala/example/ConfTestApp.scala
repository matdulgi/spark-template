package example

import core.common.{Env, SparkBase}
import core.util.SqlUtil
import example.entity.{Conf, Test}
import org.apache.spark.sql.SparkSession
import pureconfig.generic.auto._

object ConfTestApp extends SparkBase {
    override def driver(session: SparkSession, args: Array[String]): Unit = {
        val rows = SqlUtil.select("select value from test", rs => {
            Test(rs.getInt("value"))
        })

        logger.info("{}", rows(0))

        implicit val hint = Env.buildConfigHint[Conf]()
        val conf = Env.getConfigOrThrow[Conf]()

        val rows2 = SqlUtil.select(s"select value from ${conf.tableName}", rs => {
            Test(rs.getInt("value"))
        })

        logger.info("{}", rows2(0))
    }
}
