package cn.hdj.server;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

//@SpringBootTest
class FeignClientApplicationTests {

	@Test
	void contextLoads() {

		KeyPair pair = SecureUtil.generateKeyPair("RSA");
		System.out.println(Base64.encode(pair.getPrivate().getEncoded()));
		System.out.println(Base64.encode(pair.getPublic().getEncoded()));
	}

}
