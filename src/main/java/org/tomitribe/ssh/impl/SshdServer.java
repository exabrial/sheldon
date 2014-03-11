package org.tomitribe.ssh.impl;

import java.io.File;
import java.io.IOException;

import org.apache.openejb.loader.SystemInstance;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

public class SshdServer {

	private SshServer sshServer;
	
	private static final String KEY_NAME = SystemInstance.get().getOptions().get("openejb.server.ssh.key", "ssh-key");

	public void start() {
		sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(2222);
        sshServer.setHost("0.0.0.0");

        final String basePath = SystemInstance.get().getBase().getDirectory().getAbsolutePath();
        if (SecurityUtils.isBouncyCastleRegistered()) {
            sshServer.setKeyPairProvider(new PEMGeneratorHostKeyProvider(new File(basePath, KEY_NAME + ".pem").getPath()));
        } else {
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(basePath, KEY_NAME + ".ser").getPath()));
        }

        sshServer.setShellFactory(new TomEEShellFactory());
        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
			
			@Override
			public boolean authenticate(String username, String password, ServerSession session) {
				return username != null && password != null && password.equals(username);
			}
		});


        try {
            sshServer.start();
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
	
	public void stop() {
		try {
			sshServer.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}