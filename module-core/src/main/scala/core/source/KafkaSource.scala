package core.source

import core.util.KafkaUtil
import org.apache.spark.sql.SparkSession

object KafkaSource {

    /**
     * kafka를 stream 방식으로 read
     * @param session
     * @param bootstrapServers
     * @param topic
     * @param options
     * @return
     */
    def readStream(session: SparkSession,
                   bootstrapServers: String,
                   topic: String,
                   options: Map[String, String] = Map()) = {
        val df = session.readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", bootstrapServers)
          .option("subscribe", topic)
          .option("startingOffsets", "latest")
          .options(options)
          .load()
        df
    }

    /**
     * kafka를 batch 방식으로 read
     * @param session
     * @param bootstrapServers
     * @param topic
     * @param options
     * @return
     */
    def read(session: SparkSession,
             bootstrapServers: String,
             topic: String,
             startingTimestamp: Long,
             endingTimestamp: Long = java.time.Instant.now.toEpochMilli,
             options: Map[String, String] = Map()) = {

        def mkJson(timestamp: Long, `else`: Int) = {
            val partitionWithOffset = KafkaUtil.getAdminClient.getOffsetByTimestamp(topic, timestamp).map { item =>
                val (partition, offset) = item
                s""""$partition": ${offset.getOrElse(`else`)}"""
            }.mkString(",")

            s"""{"$topic": {$partitionWithOffset}}"""
        }

        val df = session.read
          .format("kafka")
          .option("kafka.bootstrap.servers", bootstrapServers)
          .option("subscribe", topic)
          .option("startingOffsets", mkJson(startingTimestamp, -2))
          .option("endingOffsets", mkJson(endingTimestamp, -1))
          .options(options)
          .load()
        df
    }
}
