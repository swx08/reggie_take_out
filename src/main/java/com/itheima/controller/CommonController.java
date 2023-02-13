package com.itheima.controller;

import com.itheima.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * ClassName:CommonController
 * Package:com.itheima.controller
 * Description
 *
 * @Author:@wenxueshi
 * @Create:2023/2/9 - 18:19
 * @Version:v1.0
 */

/**
 * 文件上传、下载功能
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        //file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会被删除。
        //获取原始文件名
        String originalFilename = file.getOriginalFilename();//abc.jpg
        //截取后缀.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名,防止上传同名文件被覆盖
        String finalFileName = UUID.randomUUID().toString() + suffix;//skfjd.jpg

        //创建一个目录对象
        File dir = new File(basePath);
        if(!dir.exists()){
            //此目录不存在则创建
            dir.mkdirs();
        }

        //将临时文件转存到指定位置
        file.transferTo(new File(basePath + finalFileName));

        return R.success(finalFileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void downLoad(String name, HttpServletResponse response){
        //通过输入流读取指定文件中的File
        FileInputStream fileInputStream = null;
        ServletOutputStream outputStream = null;
        //设置下载格式
        response.setContentType("image/jpeg");
        try {
            fileInputStream = new FileInputStream(basePath + name);
            outputStream = response.getOutputStream();

            //读写文件
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1 ){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //关闭流
        try {
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
