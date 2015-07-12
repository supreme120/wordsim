package com.gz.dt

import org.ansj.splitWord.analysis.ToAnalysis
import org.ansj.util.FilterModifWord
import org.apache.spark.mllib.feature.Word2Vec
import org.apache.spark.{SparkContext, SparkConf}
import scala.collection.JavaConversions._
/**
 * Created by naonao on 2015/6/10.
 */
class WordSimilar {

}

object WordSimilar {
  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("wordsim")//.setMaster("local[4]")
    val sc = new SparkContext(conf)

    //英文语料
    //val rdd1 = sc.textFile(args(1),4).map(line => line.split(" ").toSeq)

    //中文语料
    val rdd = sc.wholeTextFiles(args(0),4).map(_._2)  //"hdfs://192.168.0.200:9000/data/yuliaoku/C000010"

    val stopWord = sc.textFile(args(1),4).collect().toList
    val rdd1 = rdd.map{line =>
      val terms = ToAnalysis.parse(line)
      FilterModifWord.insertStopWords(stopWord)
      val filterTerms = FilterModifWord.modifResult(terms)

      val words = for(i <- 0 until filterTerms.size()) yield filterTerms.get(i).getName
      words.mkString("\t")
    }

//    println("***********  word  ********")
//    rdd1.take(5).foreach(println)
//    println("***********  word  ********")

    val rdd2 = rdd1.map(word => word.split("\t").toSeq)
    //rdd2.take(2).foreach(println)

        val word2Vec = new Word2Vec()
        val model = word2Vec.fit(rdd2)

        val synonyms = model.findSynonyms("笔记本", 10)


        for((synonym, cosSim) <- synonyms) {
          println(s"$synonym : $cosSim")
        }

    sc.stop()
  }
}
