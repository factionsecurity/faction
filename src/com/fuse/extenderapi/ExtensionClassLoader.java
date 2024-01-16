package com.fuse.extenderapi;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.fuse.dao.AppStore;

public class ExtensionClassLoader {
	
	
	final Map<String,byte[]> map=new HashMap<>();
	private URL classURL;
	
	public void loadJarFromAppStore(AppStore app) throws IOException {
		final byte[] buffer = Base64.getDecoder().decode(app.getBase64JarFile().getBytes());
		
		try(JarInputStream is=new JarInputStream(new ByteArrayInputStream(buffer))) {
			for(;;) {
				JarEntry nextEntry = is.getNextJarEntry();
				if(nextEntry==null) break;
				final int est=(int)nextEntry.getSize();
				byte[] data=new byte[est>0? est: 1024];
				int real=0;
				for(int r=is.read(data); r>0; r=is.read(data, real, data.length-real))
					if(data.length==(real+=r)) data=Arrays.copyOf(data, data.length*2);
				if(real!=data.length) data=Arrays.copyOf(data, real);
				map.put("/"+nextEntry.getName(), data);
			}
		}
		classURL=new URL("x-buffer", null, -1, "/", new URLStreamHandler() {
			protected URLConnection openConnection(URL u) throws IOException {
				final byte[] data = map.get(u.getFile());
				if(data==null) throw new FileNotFoundException(u.getFile());
				return new URLConnection(u) {
					public void connect() throws IOException {}
					@Override
					public InputStream getInputStream() throws IOException {
						return new ByteArrayInputStream(data);
					}
				};
			}
		});
	}
	
	public URL getURL() {
		return this.classURL;
	}

}
