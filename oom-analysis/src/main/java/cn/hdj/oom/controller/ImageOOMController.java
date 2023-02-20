package cn.hdj.oom.controller;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author huangjiajian
 * @Date 2023/2/7 16:13
 */
@RestController
@RequestMapping(value = "/images")
public class ImageOOMController {


    @PostMapping(value = "/addWaterMark")
    public void addWaterMark(@RequestParam(value = "file") MultipartFile file, HttpServletResponse response) throws IOException {
        System.out.println("总内存:" + (RuntimeUtil.getTotalMemory() / 1024 / 1024));
        System.out.println("剩余内存1111:" + (RuntimeUtil.getFreeMemory() / 1024 / 1024));
        //水印
        InputStream wuxiaoWaterMark = ResourceUtil.getStream("print_wuxiao.png");
        try {
            ImgUtil.pressImage(
                    IoUtil.toBuffered(file.getInputStream()),
                    IoUtil.toBuffered(response.getOutputStream()),
                    ImgUtil.read(wuxiaoWaterMark), //水印图片
                    0, //x坐标修正值。 默认在中间，偏移量相对于中间偏移
                    0, //y坐标修正值。 默认在中间，偏移量相对于中间偏移
                    0.1f
            );

            System.out.println("剩余内存222:" + (RuntimeUtil.getFreeMemory() / 1024 / 1024));
        } finally {
            IoUtil.close(file.getInputStream());
            IoUtil.close(response.getOutputStream());
            IoUtil.close(wuxiaoWaterMark);
        }
        System.out.println("=========================================================");
    }

    @PostMapping(value = "/addWaterMark2")
    public void addWaterMark2(@RequestParam(value = "file") MultipartFile file, HttpServletResponse response) throws IOException {
        System.out.println("总内存:" + (RuntimeUtil.getTotalMemory() / 1024 / 1024));
        System.out.println("剩余内存1111:" + (RuntimeUtil.getFreeMemory() / 1024 / 1024));
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image srcImage = toolkit.createImage(file.getBytes());
            int wideth = -1;
            int height = -1;
            //Toolkit加载是异步的,它有一个观察器,要等待它回加载完成才能再draw出去。
            while (true) {
                wideth = srcImage.getWidth(null); // 得到源图宽
                height = srcImage.getHeight(null); // 得到源图长
                if (wideth > 0 && height > 0) {
                    break;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            BufferedImage bufferedImage = new BufferedImage(wideth, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = bufferedImage.getGraphics();

            graphics.drawImage(srcImage, 0, 0, wideth, height, null);
            srcImage.flush();

            //水印
            URL resource = ResourceUtil.getResource("print_wuxiao.png");
            Image wuxiaoWaterMark = toolkit.createImage(resource);
            while (true) {
                int a = wuxiaoWaterMark.getWidth(null); // 得到源图宽
                int b = wuxiaoWaterMark.getHeight(null); // 得到源图长
                if (a > 0 && b > 0) {
                    break;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            graphics.drawImage(wuxiaoWaterMark, 0, 0, 200, 200, null);
            wuxiaoWaterMark.flush();
            graphics.dispose();
            ImgUtil.writeJpg(bufferedImage, response.getOutputStream());
            bufferedImage.flush();
            System.out.println("剩余内存222:" + (RuntimeUtil.getFreeMemory() / 1024 / 1024));
        } finally {
            IoUtil.close(file.getInputStream());
            IoUtil.close(response.getOutputStream());
        }

    }
}
