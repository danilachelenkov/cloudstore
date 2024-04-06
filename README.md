# ДИПЛОМНАЯ РАБОТА «Облачное хранилище»

## 1. Описание 
Серверная бэковая часть располагается на порту 8081. Сервис поддерживает обработку несколько типов запросов:
- GET
- POST
- PUT
- DELETE

Сервис подключен к Spring Security и выполняет аутентификацию пользователей, при попытке подключения. Если пользователь не прошел аутентификацию, то клиентское приложение получает ошибку. Иначе клиент получает сгенерированый Jwt-токен. Jwt-токен - генерируется на основании секретного ключа. Секркетный ключ, как и часть настроек сервиса хранится в базе данных. 
Стуктура таблиц, связи и индексы, а также наполненеи таблиц настроечныйми и тестовыми данными происходит при участии  подключенного и сконфигурированого liquibase. Конфигурационный файл имеет различные профили настроек: dev\prod\test. Настроечные данные в таблицах заполняются исходят из активного подключенного профиля сервиса, которые определеяется параметром при его запуске. Севис также предоставляет не только возможность физического удаление файлов, но и виртуального удаления с помещением записи и файла в архивную область.


### Команда запуска
```
docker-compose up
```

### 1. Метод аутентификации Login
```
RequestType = POST
url: http://localhost:8081/cloud/login
```
#### Пример json-запроса для аутентификации пользователя
```
{
    login: "testing@mail.ru", password: "123"
}
```

#### Пример json-ответа после успешной аутентификации пользователя

```
{
    "auth-token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nQG1haWwucnUiLCJpYXQiOjE3MTIzOTkxNzMsImV4cCI6MTczMjM5OTE3M30.EGz3uEsGIOegyXjfLlsOzZ3WPR7Z5yyLnnUONO85uN8"
}
```

### 2. Метод деаутентификации Logout при завершении работы 
```
RequestType = POST
url: http://localhost:8081/cloud/logout
```
#### Параметры запроса
```
Accept:
application/json, text/plain, */*
Auth-Token:
Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nQG1haWwucnUiLCJpYXQiOjE3MTIzOTkxNzMsImV4cCI6MTczMjM5OTE3M30.EGz3uEsGIOegyXjfLlsOzZ3WPR7Z5yyLnnUONO85uN8
Referer:
http://localhost:8080/
Sec-Ch-Ua:
"Google Chrome";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
Sec-Ch-Ua-Mobile:
?0
Sec-Ch-Ua-Platform:
"Windows"
User-Agent:
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36
```
#### Параметры ответа
```
Request URL:
http://localhost:8081/cloud/logout
Request Method:
POST
Status Code:
200 OK
Remote Address:
[::1]:8081
Referrer Policy:
strict-origin-when-cross-origin
```

### 3. Метод GetListFilesByLimit получения списка файлов для авторизованного пользователя 
```
RequestType = GET
url: http://localhost:8081/cloud/list?limit=3
```
#### RequestHeader
```
Accept:
application/json, text/plain, */*
Auth-Token:
Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nQG1haWwucnUiLCJpYXQiOjE3MTIzOTk2MzAsImV4cCI6MTczMjM5OTYzMH0.xyht0Q_qYDptpWzuuBePsxJkygqxYYBerlAE9F4bnOk
Referer:
http://localhost:8080/
Sec-Ch-Ua:
"Google Chrome";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
Sec-Ch-Ua-Mobile:
?0
Sec-Ch-Ua-Platform:
"Windows"
User-Agent:
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36
```
#### Пример json-ответа по запросу списка файлов аутентифицированного пользователя
```
[
    {"filename":"UNO.pdf","size":327650},
    {"filename":"Kasper.jpg","size":294810}
]
```

### 4. Метод AddFile добавления файла в хранилище
```
RequestType = POST
http://localhost:8081/cloud/file?filename=Kasper.jpg

Body = (MultipartFile) file
```
#### Пример ответа в рузультате успешного сохранения
```
Success upload
```

### 5. Метод PutFile  изменения наименования добавленного файла в хранилище
```
RequestType = PUT
http://localhost:8081/cloud/file?filename=Kasper.jpg
```
#### Пример json-запроса для изменения наименования файла
```
{
   {filename: "979.jpg"}
}
```

#### Пример ответа после успешного изменения наименования файла

```
File name to upload
```

### 6. Метод  GetFile выгрузки ранее добавленного файла в хранилище
```
RequestType = GET
http://localhost:8081/cloud/file?filename=979.jpg
```
#### Пример header-запроса для изменения наименования файла
```
Accept:
application/json, text/plain, */*
Auth-Token:
Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nQG1haWwucnUiLCJpYXQiOjE3MTIzOTk2MzAsImV4cCI6MTczMjM5OTYzMH0.xyht0Q_qYDptpWzuuBePsxJkygqxYYBerlAE9F4bnOk
Referer:
http://localhost:8080/
Sec-Ch-Ua:
"Google Chrome";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
Sec-Ch-Ua-Mobile:
?0
Sec-Ch-Ua-Platform:
"Windows"
User-Agent:
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36

```
#### Пример json-ответа выгруженного файла
```
byte[] file
```

### 7. Метод  DoDelete изменения наименования добавленного файла в хранилище
```
RequestType = DELETE
http://localhost:8081/cloud/file?filename=979.jpg
```
#### Пример header-запроса для изменения наименования файла
```
Accept:
application/json, text/plain, */*
Auth-Token:
Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nQG1haWwucnUiLCJpYXQiOjE3MTIzOTk2MzAsImV4cCI6MTczMjM5OTYzMH0.xyht0Q_qYDptpWzuuBePsxJkygqxYYBerlAE9F4bnOk
Referer:
http://localhost:8080/
Sec-Ch-Ua:
"Google Chrome";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
Sec-Ch-Ua-Mobile:
?0
Sec-Ch-Ua-Platform:
"Windows"
User-Agent:
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36
```

#### Пример ответа после успешного изменения наименования файла

```
Request URL:
http://localhost:8081/cloud/file?filename=979.jpg
Request Method:
DELETE
Status Code:
200 OK
Remote Address:
[::1]:8081
Referrer Policy:
strict-origin-when-cross-origin
```

```
Success deleted
```


