package yyl.study.scala.subgraph

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import java.lang

object HdfsHelper {
	val conf=new Configuration(); 
	
	val fs=FileSystem.get(conf);
	
	val TEMP_PATH="/yyl/test/temp/"
	
	/**
	 * delete the path on the hdfs
	 */
	def deletePath(path:String)={
	  val p=new Path(path)
	  
	  if(fs.exists(p))
	  {
	      fs.delete(p)
	  }
	}
	
	def isExist(path:String):Boolean=fs.exists(new Path(path))
	
	def getPath(postfix:String)=TEMP_PATH+postfix
	  
	
}