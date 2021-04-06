package com.khjxiaogu.AAAntiWorm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JarWatcher extends Thread {
	private final Map<String,byte[]> data=new ConcurrentHashMap<>();
	private final Map<String,byte[]> hash=new ConcurrentHashMap<>();
	private WatchKey wk;
	private final static String prefix="[AntiWorm]";
	private final File parent;
	private static class WriterThread extends Thread{
		byte[] data;
		File towrite;

		@Override
		public void run() {
			while (true) {
				try (FileOutputStream fos = new FileOutputStream(towrite, false)) {
					fos.write(data);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				System.out.println(prefix+"Protected "+towrite.getName());
				return;
			}
		}
		public WriterThread(byte[] data, File towrite) {
			this.data = data;
			this.towrite = towrite;
		}
	}
	private static class DeleteThread extends Thread{
		File todel;
		@Override
		public void run() {
			while (true) {
				try{
					if(todel.delete()) {
						System.out.println(prefix+"Removed "+todel.getName());
						return;
					}
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					continue;
				}

			}
		}
		public DeleteThread(File todel) {
			this.todel = todel;
		}
	}
	JarWatcher(File file) {
		parent=file;
		for(File f:file.listFiles()) {
			if(f.getName().endsWith(".jar")){
				byte[] datax;
				try(FileInputStream fis=new FileInputStream(f)){
					datax = readAll(fis);
					data.put(f.getName(),datax);
					hash.put(f.getName(),SHA256(datax));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			wk=Paths.get(file.toURI()).register(watcher,StandardWatchEventKinds.ENTRY_MODIFY,StandardWatchEventKinds.ENTRY_CREATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		System.out.println(file.getAbsolutePath());
	}
	private byte[] readAll(InputStream i) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
		int nRead;
		byte[] data = new byte[4096];

		try {
			while ((nRead = i.read(data, 0, data.length)) != -1)
				ba.write(data, 0, nRead);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}

		return ba.toByteArray();
	}
	private byte[] SHA256(byte[] datax) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");

			return digest.digest(datax);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Warning!SHA-256 does not works!");
		}
		return new byte[0];
	}
	@Override
	public void run() {
		super.run();
		while(true) {
			try {
				for(WatchEvent<?> x:wk.pollEvents()) {
					Path p=(Path) x.context();
					Object kind=x.kind();
					File f=new File(parent,p.toFile().getName());
					if(kind==StandardWatchEventKinds.ENTRY_MODIFY) {
						if(f.getName().endsWith(".jar")) {
							try(FileInputStream fis=new FileInputStream(f)){
								byte[] hs=SHA256(readAll(fis));
								String name=p.toFile().getName();
								if(!Arrays.equals(hs,hash.get(name))) {
									System.out.println("Modification founded!");
									new WriterThread(data.get(name),f).start();
								}
							}catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}else if(kind==StandardWatchEventKinds.ENTRY_CREATE) {
						if(f.getName().endsWith(".jar")) {
							if(!hash.containsKey(f)) {
								new DeleteThread(f).start();
							}
						}
					}
				}
				wk.reset();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
