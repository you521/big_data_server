package com.kafka.huaxia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.dstream.DStream;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import scala.Tuple2;

public class JavaSparkStream
{

    public static void main(String[] args)
    {
       System.out.println("================计算开始=================");
       // 创建sparkConf对象，并设置参数
       SparkConf sparkConf = new SparkConf();
       sparkConf.setAppName("streamWordCount");
       sparkConf.setMaster("local[2]");
       // 是让streaming任务可以优雅的结束，当把它停止掉的时候，它会执行完当前正在执行的任务
       sparkConf.set("spark.streaming.stopGracefullyOnShutdown","true");
       // 开启后spark自动根据系统负载选择最优消费速率
       sparkConf.set("spark.streaming.backpressure.enabled","true");
       sparkConf.set("spark.default.parallelism", "6");
       JavaSparkContext sc = new JavaSparkContext(sparkConf);
       sc.setLogLevel("WARN");
       // 每隔10秒钟，sparkStreaming作业就会收集最近5秒内的数据源接收过来的数据
       JavaStreamingContext jssc = new JavaStreamingContext(sc,Durations.seconds(10));
       //checkpoint目录
       //jssc.checkpoint(ConfigurationManager.getProperty(Constants.STREAMING_CHECKPOINT_DIR));
       jssc.checkpoint("E://Spark//data//dataresult//streaming_checkpoint");
       
       // 构建kafka参数map
       // 主要要放置的是连接的kafka集群的地址（broker集群的地址列表）
       Map<String, Object> kafkaParams = new HashMap<>();
       //Kafka服务监听端口,当是多个kafka服务器时,用逗号隔开
       kafkaParams.put("bootstrap.servers", "192.168.8.111:9092,192.168.8.111:9093,192.168.8.111:9094");
       //指定kafka输出key的数据类型及编码格式（默认为字符串类型编码格式为uft-8）
       kafkaParams.put("key.deserializer", StringDeserializer.class);
       //指定kafka输出value的数据类型及编码格式（默认为字符串类型编码格式为uft-8）
       kafkaParams.put("value.deserializer", StringDeserializer.class);
       //消费者ID，随意指定
       kafkaParams.put("group.id","consumer");
       //指定从latest还是smallest(最早)处开始读取数据
       //latest 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
       kafkaParams.put("auto.offset.reset", "latest");
       //如果true,consumer定期地往zookeeper写入每个分区的offset
       kafkaParams.put("enable.auto.commit", true);

       // 构建topic set
       String kafkaTopics = "test";
       String[] kafkaTopicsSplited = kafkaTopics.split(",");

       Collection<String> topics = new HashSet<>();
       for (String kafkaTopic : kafkaTopicsSplited) {
           topics.add(kafkaTopic);
       }
       try
       {
            // 通过KafkaUtils.createDirectStream(...)获得kafka数据，kafka相关参数由kafkaParams指定
            JavaInputDStream<ConsumerRecord<String, String>> lines =  KafkaUtils.createDirectStream(
                            jssc,
                            LocationStrategies.PreferConsistent(),
                            ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                    );
            System.out.println("=========================执行了吗======================");
            System.out.println("==================lines====================="+lines);
            // 获取words
            // flatMap将DStream的一个元素给拆分成多个元素；FlatMapFunction的两个参数分别是输入和输出类型
            JavaDStream<String> words = lines.flatMap(new FlatMapFunction<ConsumerRecord<String, String>, String>(){
                private static final long serialVersionUID = 1L;  
                
                @Override 
                public Iterator<String> call(ConsumerRecord<String, String> line) throws Exception {
                    List<String> list = new ArrayList<>();
                    // 获取到kafka的每条数据 进行操作
                    System.out.print("***************************" + line.value() + "***************************");
                    list.add(line.value());
                    return list.iterator();
                  } 
            });
            System.out.println("====================words================"+words);
            // 获取word,将每一个单词，映射为(单词, 1)的这种格式，为之后进行reduce操作做准备 
            // mapToPair，就是将每个元素映射为一个(v1,v2)这样的Tuple2类型的元素 
            // mapToPair得与PairFunction函数配合使用，PairFunction中的第一个泛型参数代表输入类型 
            // 第二个和第三个泛型参数，代表的输出的Tuple2元素的第一个值和第二个值的类型
            // JavaPairRDD的两个泛型参数，分别代表了tuple元素的第一个值和第二个值的类型 
            JavaPairDStream<String , Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() { 
                private static final long serialVersionUID = 1L; 
                @Override 
                public Tuple2<String, Integer> call(String word) throws Exception {
                    System.out.print("***************************" + word + "***************************");
                    return new Tuple2<>(word, 1);                    
                  }
                });
            
            // reduce操作（原理同MapReduce的reduce操作一样）
            // 以单词作为key，统计每个单词出现的次数 
            // 最后返回的JavaPairRDD中的元素，也是tuple，但是第一个值就是每个key，第二个值就是key出现的次数
            JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() { 
                  private static final long serialVersionUID = 1L; 
                  @Override
                  public Integer call(Integer v1, Integer v2) throws Exception {
                      return v1 + v2;                  
                  }
              }); 
            wordCounts.print();
            System.out.println("===================================="+wordCounts);
            
            //历史累计 60秒checkpoint一次
            DStream<Tuple2<String, Integer>> result = pairs.updateStateByKey(((Function2<List<Integer>, Optional<Integer>, Optional<Integer>>) (values, state) -> {
                Integer updatedValue = 0;
                // 如果state之前已经存在，那么这个key可能之前已经被统计过，否则说明这个key第一次出现
                if (state.isPresent()) {
                    updatedValue = Integer.parseInt(state.get().toString());
                }
                // 更新state
                for (Integer value : values) {
                    updatedValue += value;  
                }
                return Optional.of(updatedValue);
            })).checkpoint(Durations.seconds(60));

            result.print();

            //开窗函数 10秒计算一次 计算前15秒的数据聚合
            JavaPairDStream<String, Integer> result2 = pairs.reduceByKeyAndWindow((Function2<Integer, Integer, Integer>) (x, y) -> x + y,
                    Durations.seconds(30), Durations.seconds(10));
            result2.print();

            jssc.start();
            jssc.awaitTermination();
            jssc.stop();
            jssc.close();
        } catch (Exception e)
        {
           System.out.println("=======================抛出异常======================");
           e.printStackTrace();
        }
           
        }

}
