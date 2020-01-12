<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>

<IMG STYLE="position:absolute; TOP:35px; LEFT:570px;" SRC="ff.jpg">

<p style="color:red;font-family:courier;font-size:22px ;position:absolute; bottom:270px; LEFT:470px;">Multiple files are found for the same,</p>
<p style="color:red;font-family:courier;font-size:22px ;position:absolute; bottom:250px; LEFT:470px;"> account number. Program is terminated.</p>
 
 <div style="margin-left: 90px;">
		<table class='table'>
	<tbody>
<tr><td>
<input type="text" class="form-control" name="" id="" style="position:absolute" placeholder="Enter Employee Code"  value="<%out.print("file"); %>" readonly="readonly">
</td></tr>
</tbody></table>
</div>
  



<button style="position:absolute; bottom:230px; LEFT:670px;" onclick="goBack()">Go Back</button>


</body>
</html>