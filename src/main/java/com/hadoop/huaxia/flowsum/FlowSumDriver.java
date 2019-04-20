package com.hadoop.huaxia.flowsum;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * 
    * @ClassName: FlowSumDriver  
    * @Description: hadoop程序的main函数,在里面定义一个Job并运行它  
    * @author you  
    * @date 2019年4月16日  
    *
 */
public class FlowSumDriver
{
    public static void main(String[] args) throws Exception{
        long timetamp = System.currentTimeMillis();
        System.out.println("===========timetamp========="+timetamp);
        //创建JobConf,通过Job来封装本次mr的相关信息
        Configuration config = new Configuration();

        config.set("mapreduce.framework.name","local");
        Job job = Job.getInstance(config);

        //指定本次mapreduce job jar包运行主类
        job.setJarByClass(FlowSumDriver.class);

        //指定本次mapreduce 所用的mapper reducer类分别是什么
        job.setMapperClass(FlowSumMapper.class);
        job.setReducerClass(FlowSumReducer.class);

        //指定本次mapreduce mapper阶段的输出  k  v类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(FlowBean.class);

        //指定本次mr 最终输出的 k v类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FlowBean.class);

        //指定本次mr 输入的数据路径 和最终输出结果存放在什么位置
        FileInputFormat.setInputPaths(job,"F:\\hadoop\\hdfs\\data\\test.txt");
        FileOutputFormat.setOutputPath(job,new Path("F:\\hadoop\\hdfs\\flowsum\\output"));
        //FileInputFormat.setInputPaths(job,"/wordcount/input");
        //FileOutputFormat.setOutputPath(job,new Path("/wordcount/output"));

        // job.submit(); //一般不要这个.
        //提交程序  并且监控打印程序执行情况
        boolean b = job.waitForCompletion(true);
        System.out.println("===========b==========="+b);
        System.out.println("花费时间：" + (System.currentTimeMillis() - timetamp));
        System.exit(b?0:1);
    }
}
