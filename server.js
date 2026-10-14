const http = require('http');

const server = http.createServer((req, res) => {
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(`<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="UTF-8">
  <title>مرحباً</title>
  <style>
    body {
      font-family: system-ui, sans-serif;
      background: #0f172a;
      color: #f8fafc;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      margin: 0;
    }
    h1 { font-size: 3rem; }
  </style>
</head>
<body>
  <h1>مرحباً بك 👋</h1>
</body>
</html>`);
});

server.listen(3000, () => {
  console.log('الخادم يعمل على http://127.0.0.1:3000');
});
