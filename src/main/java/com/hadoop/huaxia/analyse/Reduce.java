package com.hadoop.huaxia.analyse;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.hadoop.huaxia.flowsum.FlowBean;

import java.io.IOException;

public class  Reduce extends Reducer<Text,FlowBean,Text,FlowBean>{

    FlowBean flowBean = new FlowBean();

    protected void reduce(Text key, Iterable<FlowBean> values, Reducer.Context context) throws IOException, InterruptedException {

        for(FlowBean bean:values){
            //bean.getDistrict();
            bean.getDownFlow();
        }
        //flowBean.set(upFlowCount,downFlowCount);
        context.write(key,flowBean);
    }
}
