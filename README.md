
Usage
--------------
Запуск новой задачи
--------------
```
curl -X POST -H "Content-Type: text/plain" -d @SCRIPT_FILE http://localhost:8080/task?blocked=1
```
SCRIPT_FILE - Файл Javascript. 

В неблокирующем режиме возвращается
```
HTTP/1.1 201 Created
Location: /task/f9d4092f-a614-4c58-96f7-8a1e0b564078
```
В блокирующем возвращается вывод скрипта

Список задач
--------------
```
curl -X GET http://localhost:8080/task?satge=DoneOk
```
satge может быть: Pending, InProgress, DoneOk, DoneError, Interrupted

Получение информации о задаче
--------------
```
curl -X GET http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078
```
f9d4092f-a614-4c58-96f7-8a1e0b564078 - id задачи

Принудительное завершение задачи
--------------
```
curl -X DELETE http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078
```
