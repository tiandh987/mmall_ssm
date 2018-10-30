package com.tiandh.service.impl;

import com.google.common.collect.Lists;
import com.tiandh.service.IFileService;
import com.tiandh.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FileService implements IFileService {

//    private Logger logger = LoggerFactory.getLogger(FileService.class);

    @Override
    public String upload(MultipartFile file, String path) {
        //获取上传文件的原始文件名
        String fileName = file.getOriginalFilename();
        //获取上传文件的扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        //长传后的文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        log.info("开始上传文件，上传文件的文件名：{}，上传的路径是：{}，新文件名是：{}",fileName,path,uploadFileName);

        //创建上传文件夹
        File fileDir = new File(path);
        if (!fileDir.exists()){
            //文件夹不存在
            //赋予写权限
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile = new File(path,uploadFileName);
        try {
            //transferTo()是springmvc封装的方法，用于图片上传时，把内存中图片写入磁盘
            file.transferTo(targetFile);
            //文件上传成功

            //将targetFile上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //上传完之后，删除upload下面的文件
            targetFile.delete();
        } catch (IOException e) {
            log.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
