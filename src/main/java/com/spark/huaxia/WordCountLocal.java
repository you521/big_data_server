package com.spark.huaxia;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;

public class WordCountLocal implements Serializable
{

    private static final String OUTPUTFILEPATH = "E://Spark//data//dataresult";
    
    private static final long serialVersionUID = -7114915627898482737L;

    public static void main(String[] args)  
    {
        // 1 创建一个sparkconf 对象并配置
        // 使用setMaster 可以设置spark集群可以链接集群的URL，如果设置local 代表在本地运行而不是在集群运行
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local");
        sparkConf.setAppName("wordCountLocal");
        
        // 2 创建javasparkContext对象
        // sparkcontext 是一个入口，主要作用就是初始化spark应用程序所需的一些核心组件，例如调度器，task，
        // 还会注册spark，sparkMaster结点上注册。反正就是spake应用中最重要的对象
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);
        
        // 3 要针对输入源（hdfs文件、本地文件，等等），创建一个初始的RDD
        // 输入源中的数据会打乱，分配到RDD的每个partition中，从而形成一个初始的分布式的数据集 
        // 在Java中，创建的普通RDD，都叫做JavaRDD 
        // RDD中，有元素这种概念，如果是hdfs或者本地文件创建的RDD，每一个元素就相当于 是文件里的一行
        // textFile()用于根据文件类型的输入源创建RDD
        JavaRDD<String> lines = javaSparkContext.textFile("E://Spark//data//a.txt");
        
        // 4 对初始RDD进行transformation操作，也就是一些计算操作 注意，RDD支持两种类型的操作
        // 一种是转化（transformation）操作， 一种是行动（action）操作
        // flatMap将RDD的一个元素给拆分成多个元素；FlatMapFunction的两个参数分别是输入和输出类型
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() { 
            private static final long serialVersionUID = 1L;  
            @Override 
            public Iterator<String> call(String line) throws Exception {
               // 放到迭代器中
               return Arrays.asList(line.split(" ")).iterator(); 
              } 
          });
        
        // 5 将每一个单词，映射为(单词, 1)的这种格式，为之后进行reduce操作做准备 
        // mapToPair，就是将每个元素映射为一个(v1,v2)这样的Tuple2（scala里的Tuple类型）类型的元素 
        // mapToPair得与PairFunction函数配合使用，PairFunction中的第一个泛型参数代表输入类型 
        // 第二个和第三个泛型参数，代表的输出的Tuple2元素的第一个值和第二个值的类型
        // JavaPairRDD的两个泛型参数，分别代表了tuple元素的第一个值和第二个值的类型 
        JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() { 
           private static final long serialVersionUID = 1L; 
           @Override 
           public Tuple2<String, Integer> call(String word) throws Exception {
               return new Tuple2<>(word, 1);                    
             }
           });
        
        // 6 reduce操作（原理同MapReduce的reduce操作一样）
        // 以单词作为key，统计每个单词出现的次数 
        // 最后返回的JavaPairRDD中的元素，也是tuple，但是第一个值就是每个key，第二个值就是key出现的次数
        JavaPairRDD<String , Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() { 
              private static final long serialVersionUID = 1L; 
              @Override
              public Integer call(Integer v1, Integer v2) throws Exception {
                  return v1 + v2;                  
              }
          });
        
        // 7 触发程序执行 
        // 到这里为止，我们通过几个Spark算子操作，已经统计出了单词的次数 
        // 目前为止我们使用的flatMap、mapToPair、reduceByKey操作，都是transformation操作
        // 现在我们需要一个action操作来触发程序执行（这里是foreach）
        wordCounts.foreach(new VoidFunction<Tuple2<String, Integer>>() { 
            private static final long serialVersionUID = 1L; 
            @Override 
            public void call(Tuple2<String, Integer> wordCount) throws Exception { 
                System.out.println(wordCount._1 + " 出现了 " + wordCount._2 + " 次; "); 
               } 
            });
        
        // 我们也可以通过将统计出来的结果存入文件（这也是一个action操作），来触发之前的transformation操作 
        try { 
            wordCounts.saveAsTextFile(OUTPUTFILEPATH); 
        } catch (Exception e) 
        { 
           e.printStackTrace();
           System.out.println("=======================抛出异常=======================");
        }
       
        javaSparkContext.close();
     }

}
