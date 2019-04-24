package com.hadoop.huaxia;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HdfsFileToLocal
{
    private static String String_MASTER_URI = "hdfs://127.0.0.1:9000";
    
    public static void main(String[] args) throws URISyntaxException, IOException
    {
        // hdfs文件路径
        //String source = "/hdfs-data/b.txt";
        // 下载全部文件时，保存所有文件的文件夹
        String source = "/hdfs-data";
        // 将要保存的本地路径
        String dest = "E://Spark/hdfs";
        //downloadFile(source, dest);
        downloadAllFile(source, dest);
    }

    /**
     * 
        * @Title: downloadFile  
        * @Description: 从hdfs文件系统下载单个文件  
        * @param @param source 文件来源路径
        * @param @param dest 将要保存文件的路径
        * @param @throws URISyntaxException
        * @param @throws IOException    参数  
        * @return void    返回类型  
        * @throws
     */
    public static void downloadFile(String source, String dest) throws URISyntaxException, IOException
    {
        // 读取hadoop文件系统
        Configuration configuration = new Configuration();
        URI uri = new URI(String_MASTER_URI);
        // FileSystem是用户操作HDFS的核心类，它获得URI对应的HDFS文件系统
        FileSystem fileSystem = FileSystem.get(uri,configuration);
        // 源文件路径
        Path srcPath = new Path(source);
        // 目的文件路径
        Path destPath = new Path(dest);
        File file = new File(dest);
        // 查看目的路径是否存在
        if(!file.exists())
        {
            // 如果路径不存在，即刻创建
            file.mkdirs();
        }
        // 得到本地文件名称
        String filename = source.substring(source.lastIndexOf('/') + 1,source.length());
        try
        {
            fileSystem.copyToLocalFile(srcPath, destPath);
            System.out.println("文件名称为"+" "+filename+" "+"已经下载成功");
            FileStatus[] fileStatus  = fileSystem.listStatus(new Path("/hdfs-data"));
            for(FileStatus files:fileStatus){
               // 打印出当前上传文件目录下的所有的文件路径 
               System.out.println(files.getPath());
           }
        } catch (Exception e)
        {
            System.err.println("Exception caught :" + e);
            System.exit(1);
         } finally {
            fileSystem.close();
         }
    }
    
    /**
     * 
        * @Title: downloadAllFile  
        * @Description: 下载hdfs文件下的所有文件  
        * @param @param source
        * @param @param dest
        * @param @throws URISyntaxException
        * @param @throws IOException    参数  
        * @return void    返回类型  
        * @throws
     */
    public static void downloadAllFile(String source, String dest) throws URISyntaxException, IOException
    {
        // 读取hadoop文件系统
        Configuration configuration = new Configuration();
        URI uri = new URI(String_MASTER_URI);
        // FileSystem是用户操作HDFS的核心类，它获得URI对应的HDFS文件系统
        FileSystem fileSystem = FileSystem.get(uri,configuration);
        // 源文件路径
        Path srcPath = new Path(source);
        // 目的文件路径
        Path destPath = new Path(dest);
        File file = new File(dest);
        // 查看目的路径是否存在
        if(!file.exists())
        {
            // 如果路径不存在，即刻创建
            file.mkdirs();
        }
        try
        {
            FileStatus[] fileStatus  = fileSystem.listStatus(srcPath);
            for(FileStatus files:fileStatus){
               // 打印出当前上传文件目录下的所有的文件路径 
               System.out.println(files.getPath());
               // 获取每个文件的地址
               Path url = files.getPath();
               fileSystem.copyToLocalFile(url, destPath);
               // 得到本地文件名称
               String filename = url.toString().substring(url.toString().lastIndexOf('/') + 1,url.toString().length());
               System.out.println("文件名称为"+" "+filename+" "+"已经下载成功");
           }
            
        } catch (Exception e)
        {
            System.err.println("Exception caught :" + e);
            System.exit(1);
         } finally {
            fileSystem.close();
         }
    }
}
