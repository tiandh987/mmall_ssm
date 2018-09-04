<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
    <h2>Hello World!</h2>

    <h2>springmvc上传文件</h2>
    <form action="/manage/product/upload.do" enctype="multipart/form-data" method="post">
        <input type="file" name="upload_file"/>
        <input type="submit" value="上传文件">
    </form>

    <h2>富文本图片上传文件</h2>
    <form action="/manage/product/richtext_img_upload.do" enctype="multipart/form-data" method="post">
        <input type="file" name="upload_file"/>
        <input type="submit" value="上传文件">
    </form>

</body>
</html>
