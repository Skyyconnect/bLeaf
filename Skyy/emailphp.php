<?php                
  $emailsubject ='Customer Information ';
  
  $webMaster = 'elleconnect.com';
  
  /* gathering data*/
  
  $emailfield = $_post['email'];
  
  $namefield = $_post['name'];
  
  $phonefield = $_post['phone'];
  $comments = $_post['comments']
                      
  $body = <<<EOD
  <br><hr><br>
  Email:$emial <br>
  Name:$name <br>
  Phone: $phone <br>
  Comments: $comments <br>
   EOD;
  
$headers = "From:$email\r\n";
$headers .= "Content-type: text/html\r\n";
$success = mail($webMaster, $emailsubject, $body, $headers);

/* results rendenderd*/

$theResults = <<<EOD
<html>
<title> email complete</title>   
<head>
<p> Thank you for your interest. elle will connect to you shortly </p>
</head>
</html>

EOD;

echo "$theResults";
 
  
                
?>