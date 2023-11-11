package com.fuse.authentication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class InsecureSSLSocketFactory extends SSLSocketFactory {

    private static final AtomicReference<InsecureSSLSocketFactory > defaultFactory = new AtomicReference<>();

    private SSLSocketFactory sf;

    public InsecureSSLSocketFactory (){
    	
        SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null,new TrustManager[] { new DummyTrustmanager() }, new SecureRandom());
			sf = ctx.getSocketFactory();
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
 
    }

    public static SocketFactory getDefault() {
    	return new InsecureSSLSocketFactory();
    }
    
    @Override
    public String[] getDefaultCipherSuites() {
        return sf.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sf.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(final String s, final int i) throws IOException {
        return sf.createSocket(s, i);
    }

    @Override
    public Socket createSocket(final String s, final int i, final InetAddress inetAddress, final int i1) throws IOException {
        return sf.createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int i) throws IOException {
        return sf.createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int i, final InetAddress inetAddress1, final int i1) throws IOException {
        return sf.createSocket(inetAddress, i, inetAddress1, i1);
    }

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return sf.createSocket(s, host, port, autoClose);
	}
}