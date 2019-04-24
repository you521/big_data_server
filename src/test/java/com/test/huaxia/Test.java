package com.test.huaxia;

import java.util.Collection;
import java.util.HashSet;

public class Test
{

    public static void main(String[] args)
    {
       // 构建topic set
        String kafkaTopics = "you";
        String[] kafkaTopicsSplited = kafkaTopics.split(",");

        Collection<String> topics = new HashSet<>();
        for (String kafkaTopic : kafkaTopicsSplited) {
            System.out.println("===========kafkaTopic==========="+kafkaTopic);
        }

    }

}
