package org.konghao.aujaker.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.konghao.aujaker.kit.CommonKit;
import org.springframework.stereotype.Service;

@Service
public class Configservice implements IConfigservice {

	@Override
	public void generateApplicatoinPropertiesByProp(String path, String propFile) {
		Map<String,String> configs = readConfigByProperties(propFile);
		generateApplicationPropertiesByMap(path,configs);
	}

	private void generateApplicationPropertiesByMap(String path, Map<String, String> configs) {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = null;
		FileWriter fw = null;
		try {
			String database = configs.get("{dataType}");
			System.out.println(database);
			String tfile = null;
			if("mysql".equals(database)) {
				tfile = "application_mysql.templates";
			} else if("sqlite3".equals("database")) {
				tfile = "application_sqlite3.templates";
				writeSqlite3Dialect(path,configs.get("{artifactId}"));
			}
			br = new BufferedReader(new InputStreamReader(
						(Configservice.class.getClassLoader()
								.getResourceAsStream
								("org/konghao/aujaker/templates/"+tfile))));
			String str = null;
			while((str=br.readLine())!=null) {
				sb.append(str+"\n");
			}
			Set<String> keys = configs.keySet();
			String fileStr = sb.toString();
			for(String key:keys) {
				if(fileStr.indexOf(key)>=0) {
					if(key.equals("{package}")) {
						//如果是package，需要加.model。默认的实体类在model中
						fileStr = fileStr.replace(key, configs.get(key)+".model");
					} else {
						fileStr = fileStr.replace(key, configs.get(key));
					}
					
				}
			}
			String nPath = path+"/"+configs.get("{artifactId}")+"/src/main/resources";
			File f = new File(nPath);
			if(!f.exists()) f.mkdirs();
			fw = new FileWriter(f+"/application.properties");
			fw.write(fileStr);
 		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(br!=null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fw!=null) fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * 拷贝sqlite3的方言
	 * @param path
	 */
	private void writeSqlite3Dialect(String path,String artifactId) {
		String packages = "com.enigmabridge.hibernate.dialect";
		String pfile = packages.replace(".", "/");
		String rPath = path+"/"+artifactId+"/src/main/java/"+pfile+"/SQLiteDialect.java";
		BufferedReader br = null;
		FileWriter fw = null;
		try {
			br = new BufferedReader(new InputStreamReader
					(Configservice.class.getClassLoader().getResourceAsStream("org/konghao/aujaker/templates/SQLiteDialect.templates")));
			fw = new FileWriter(rPath);
			String str = null;
			while((str=br.readLine())!=null) {
				fw.write(str);
				fw.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(br!=null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fw!=null) fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Map<String, String> readConfigByProperties(String propFile) {
		Properties prop = CommonKit.readProperties(propFile);
		Map<String,String> cfgs = new HashMap<String,String>();
		String type = prop.getProperty("database.type");
		cfgs.put("{artifactId}", prop.getProperty("maven.artifactId"));
		cfgs.put("{dataType}", type);
		cfgs.put("{package}", prop.getProperty("package"));
		cfgs.put("{url}", prop.getProperty("database.url"));
		cfgs.put("{driver}", prop.getProperty("database.driver"));
		cfgs.put("{dataname}", prop.getProperty("database.name"));
		if(type.equals("mysql")) {
			//如果是mysql，需要读取数据库的用户名和密码
			cfgs.put("{username}", prop.getProperty("database.username"));
			cfgs.put("{password}", prop.getProperty("database.password"));
		} else if(type.equals("sqlite3")) {
		}
		
		return cfgs;
	}

	@Override
	public void generateApplicationPropertiesByXml(String path, String xmlFile) {

	}

	@Override
	public void generatePomByProp(String path, String propFile) {

	}

	@Override
	public void generatePomByXml(String path, String xmlFile) {

	}

}