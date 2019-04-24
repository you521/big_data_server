package com.hadoop.huaxia;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
/**
 * 
    * @ClassName: CopyingLocalFileToHDFS  
    * @Description: 将本地文件上传到hadoop的hdfs中  
    * @author you  
    * @date 2019年4月24日  
    *
 */
public class CopyingLocalFileToHDFS
{
    private static String String_MASTER_URI = "hdfs://127.0.0.1:9000";
    // FileSystem实例对象，即fs
    private static FileSystem fs = null;
    // FileSystem实例对象，即Local,本地文件系统
    private static FileSystem local = null;

    public static void main(String[] args) throws IOException, URISyntaxException
    {
//        String source = "E://Spark/data/b.txt";
//        // hdfs文件路径
//        String dest = "/hdfs-data";
//        copyFromLocal(source, dest);
        
         String source = "E://Spark/hdfs";
         // hdfs文件路径
         String dest = "/hdfs-data";
         CopyManyFilesToHDFS(source, dest);
    }

    public static void copyFromLocal(String source, String dest)throws IOException, URISyntaxException
    {
        // 读取hadoop文件系统的配置
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS",String_MASTER_URI);
        //URI uri = new URI(String_MASTER_URI);
        // FileSystem是用户操作HDFS的核心类，它获得URI对应的HDFS文件系统
        FileSystem fileSystem = FileSystem.get(conf);
        // 源文件路径
        Path srcPath = new Path(source);
        // 目的文件路径
        Path destPath = new Path(dest);
        // 查看目的路径是否存在
        if (!(fileSystem.exists(destPath))) {
            // 如果路径不存在，即刻创建
            fileSystem.mkdirs(destPath);
        }
        // 得到本地文件名称
        String filename = source.substring(source.lastIndexOf('/') + 1,source.length());
        try {
            // 将本地文件上传到HDFS
            fileSystem.copyFromLocalFile(srcPath, destPath);
            FileStatus[] fileStatus  = fileSystem.listStatus(destPath);
            for(FileStatus file:fileStatus){
               // 打印出当前上传文件目录下的所有的文件路径 
               System.out.println(file.getPath());
           }
           System.out.println("本地的"+" "+filename+" "+"文件,已经成功上传到hdfs文件系统中");
         } catch (Exception e) {
            System.err.println("Exception caught :" + e);
            System.exit(1);
         } finally {
            fileSystem.close();
         }
    }
    
    /**
     * 
        * @Title: CopyManyFilesToHDFS  
        * @Description: 一次性上传多个文件  
        * @param @param source
        * @param @param dest
        * @param @throws IOException
        * @param @throws URISyntaxException    参数  
        * @return void    返回类型  
        * @throws
     */
    public static void CopyManyFilesToHDFS (String source ,String dest) throws IOException, URISyntaxException {
        // 读取hadoop文件系统的配置
        Configuration conf = new Configuration();
        // HDFS 接口
        URI uri = new URI(String_MASTER_URI);
        // 获得FileSystem实例fs
        fs = FileSystem.get(uri, conf);
        // 获得FileSystem实例，即Local
        local = FileSystem.getLocal(conf);
        if (!source.endsWith("/*")) 
        {
            source += "/*";
        }
        // 所以，我们需先获取到Local，再来做复制工作
        // ^表示匹配我们字符串开始的位置               *代表0到多个字符                        $代表字符串结束的位置    
        FileStatus[] localStatus = local.globStatus(new Path(source),new RegexAcceptPathFilter("^.*txt$"));
        // 获取目录下的所有文件路径，注意FIleUtil中stat2Paths()的使用，它将一个FileStatus对象数组转换为Path对象数组
        // localStatus是FileStatus数组类型
        Path[] listedPaths = FileUtil.stat2Paths(localStatus);
        Path destPath = new Path(dest);
        for(Path path1:listedPaths){
            //将本地文件上传到HDFS
            fs.copyFromLocalFile(path1, destPath);
            System.out.println("=================文件上传成功====================");
        }
    }
    
    /**
     * @function 只接受 txt 格式的文件aa
     * @author 小讲
     *
     */
    public static class RegexAcceptPathFilter implements PathFilter {
        // 定义变量
        private final String regex;
        // 有参构造函数
        public RegexAcceptPathFilter(String regex) {
            this.regex = regex;
        }

        public boolean accept(Path path) {
            // 匹配正则表达式，这里是^.*txt$
            boolean flag = path.toString().matches(regex);
            // 如果要接收 regex 格式的文件，则accept()方法就return flag
            // 如果想要过滤掉regex格式的文件，则accept()方法就return !flag
            return flag;
        }
    }
}
