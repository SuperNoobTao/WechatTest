<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>$Title$</title>
</head>
<body>
<!--在进行文件上传时，表单提交方式一定要是post的方式，因为文件上传时二进制文件可能会很大，还有就是enctype属性，这个属性一定要写成multipart/form-data，
　　不然就会以二进制文本上传到服务器端-->
　　<form action="welcome/Upload" method="post" enctype="multipart/form-data">
  　　

  <input type="file" name="file">

  <input type="submit" value="submit">

</form>
</body>
</html>
