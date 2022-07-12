package cn.hdj.server;

import cn.hdj.server.utils.SecurityUtil;
import org.junit.jupiter.api.Test;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2022/7/11 18:15
 */
public class SecureUtilTest {

    @Test
    public void test() {
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJhknqmInap2J7anNCZqsoXZLYZtVTRt26rH8KtXOsO7am1A53nwfpjILzaWf79jVBg7nkNJQrbiEKHKHndXvZ/wq4X2/Letcoqm0E9yswOwY1GrvyD3FLlwvMT8FCscU1RCMUO+eAxMBLh+3Sb563twg1gsGoky8ZW98WNb/3AQIDAQAB";

        String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAImGSeqYidqnYntqc0Jmqyhdkthm1VNG3bqsfwq1c6w7tqbUDnefB+mMgvNpZ/v2NUGDueQ0lCtuIQocoed1e9n/Crhfb8t61yiqbQT3KzA7BjUau/IPcUuXC8xPwUKxxTVEIxQ754DEwEuH7dJvnre3CDWCwaiTLxlb3xY1v/cBAgMBAAECgYBOPNuoXwduIXPhinnv+MC+wh1ch4QtUE+sd53+1aPtBDuxRUrFuu+hapLFL948daFumCYVMeRpoMmjGw7wwuGmF1yYGQ+p1oLR+y8gM23fgPjKZC1ychm4v1s7qmDIWOirSuYKwYUUGdX7IrIf6NNI6C2I//wGL9XOixvN2Iq+kQJBANtYHlXeJ1QXHmMT0kugjKNs25WxUt92jIs/UpxMtcnq9dWDbgZ4T9cAhCflP7oiI7a3UGueT0unBt/7Zhn3Sn8CQQCggczkFL0TFK74w4vNsZBm9OwCuMk2Nn2B7Nz5tRL6PzwVmdC6M1yM5Mt2drB16Tec9XTNt6vsEtX4+cpj2P5/AkABVd0BatPUeDuQINvD6BtTF2OS2ryFbcRXSLBpETJ+IzcR8LXnxi2+QluLnqKvsGuFPkJ4FUOoU0EAdhheChpjAkEAmQmJ6UJY0NOczTXtm68c5v3J9gJoX1dvLa1BOJnMwWv/hv0ExjTonrIbyRT7xUEbtH1Y5DinJM0E1YKd1bv1UwJBAIvDzp6D/Dbd1VBh499d/behusffjIvdZMfn+JV+ngzLBs5gOhWAsZA8Tw8pk5pclrq06velvvdTADfyH3z7uCg=";

        String encrypt = SecurityUtil.encryptInStringOutBase64("你好", publicKey);

        System.out.println(encrypt);

        encrypt = "Y0NesbC5oHfDR60s4jLIF1iiMOGIWocqzGBTgp4UXA1uS3UNYt+rK3/m3kTp5WMPYcLf3bJRpqGJtrQYjBYHzDGS3P2GLZJ0Kb0QCm3c1EU0MEIMVQvSLwt4NfWnpJRhCRO6aKfvU0TPdc8XuOmyUV5ye+pwZGEpuEoUrFOrtJc=";

        String s = SecurityUtil.decryptInBase64OutString(encrypt, privateKey);
        System.out.println("55 "+s);
    }
}
