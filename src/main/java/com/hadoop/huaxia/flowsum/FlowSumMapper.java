package com.hadoop.huaxia.flowsum;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;




public class FlowSumMapper extends Mapper<LongWritable, Text, Text, FlowBean>{
    // 定义text对象
    Text text = new Text();
    // new一次对象,多次使用. 效率较高
    FlowBean flowBean = new FlowBean(); 
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        // 数据文件如果是两个连续的 \t 会有问题
        String[] fields = line.split("\t"); 

        String phoneNum = fields[1];
        // 倒着获取需要的字段
        long upFlow = Long.parseLong(fields[fields.length-3]); 
        long downFlow = Long.parseLong(fields[fields.length-2]);
        // 效率太低,每次都产生对象
        //context.write(new Text(phoneNum),new FlowBean(upFlow,downFlow)); 

        // 使用对象中的set方法来写入数据,避免大量new对象
        text.set(phoneNum);
        flowBean.set(upFlow,downFlow);
        context.write(text,flowBean);
    }
}
