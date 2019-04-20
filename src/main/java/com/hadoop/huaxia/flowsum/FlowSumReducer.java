package com.hadoop.huaxia.flowsum;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


/**
 * <手机号1, bean><手机号2, bean><手机号2, bean><手机号, bean>
 *
 *     <手机号1, bean><手机号1, bean>
 *     <手机号2, bean><手机号2, bean>
 */
public class FlowSumReducer extends Reducer<Text,FlowBean,Text,FlowBean>{

    FlowBean flowBean = new FlowBean();
    @Override
    protected void reduce(Text text, Iterable<FlowBean> values, Context context) throws IOException, InterruptedException {
        long upFlowCount = 0;
        long downFlowCount = 0;

        for(FlowBean bean:values){
            upFlowCount += bean.getUpFlow();
            downFlowCount += bean.getDownFlow();
        }
        flowBean.set(upFlowCount,downFlowCount);
        context.write(text,flowBean);
    }
}
