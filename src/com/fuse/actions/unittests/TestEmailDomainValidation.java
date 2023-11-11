package com.fuse.actions.unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fuse.utils.FSUtils;

public class TestEmailDomainValidation {

	@Test
	public void test() {
		String email = "josh.summitt@fusesoft.co";
		assertTrue(FSUtils.checkEmailDomain(email));
		email = "josh.summitt@fusesoftsecurity.com";
		assertTrue(FSUtils.checkEmailDomain(email));
		email = "josh.summitt@fusesoftsecurity.com ";
		assertTrue(FSUtils.checkEmailDomain(email));
		email = " josh.summitt@fusesoftsecurity.com";
		assertTrue(FSUtils.checkEmailDomain(email));
		email = "josh.summitt@gmail.com";
		assertTrue(!FSUtils.checkEmailDomain(email));
		email = " josh.summitt@gmail.com";
		assertTrue(!FSUtils.checkEmailDomain(email));
		email = "josh.summitt@gmail.com ";
		assertTrue(!FSUtils.checkEmailDomain(email));
	}

}
