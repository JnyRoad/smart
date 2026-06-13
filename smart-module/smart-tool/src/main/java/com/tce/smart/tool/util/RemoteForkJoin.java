package com.tce.smart.tool.util;

import jcifs.smb.SmbFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-08-03 19:05
 **/

public class RemoteForkJoin extends RecursiveTask<List<String>> {

	private final Integer TEMP_VAL = 1000;

	private List<SmbFile> smbFileList = new ArrayList<>();

	private final LocalDateTime lastTime;

	public RemoteForkJoin(List<SmbFile> smbFileList,LocalDateTime lastTime){
		this.smbFileList = smbFileList;
		this.lastTime = lastTime;
	}

	@Override
	protected List<String> compute() {
		if(smbFileList.size() < TEMP_VAL){
			List<String> fileNameList = new ArrayList<>();
			for (int i = 0;i < smbFileList.size();i++) {
				SmbFile file = smbFileList.get(i);
				long lastModified = file.getLastModified();
				LocalDateTime longToLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
				if(lastTime.isBefore(longToLocalDateTime)){
					fileNameList.add(file.getName().replace(".jpg",""));
				}
			}
			return fileNameList;
		} else {
			int middle = smbFileList.size() / 2;
			List<SmbFile> smbFiles = smbFileList.subList(0, middle - 1);
			RemoteForkJoin task1 = new RemoteForkJoin(smbFiles,lastTime);
			task1.fork();
			List<SmbFile> smbFiles2 = smbFileList.subList(middle, smbFileList.size() - 1);
			RemoteForkJoin task2 = new RemoteForkJoin(smbFiles2,lastTime);
			task2.fork();

			List<String> fileNameList = new ArrayList<>();

			List<String> join1 = task1.join();
			List<String> join2 = task1.join();
			fileNameList.addAll(join1);
			fileNameList.addAll(join2);
			return fileNameList;
		}
	}
}
