ScriptEngine
--------------

Usage
--------------
Запуск новой задачи
```
curl -X POST -H "Content-Type: application/json" -d @FILE_TASK_START_JSON http://localhost:8080/task
```
FILE_TASK_START_JSON - Файл содержащий json строку. 
Пример:
```
{"script":"print()","blocked":false}
```
В неблокирующем режиме возвращается
```
HTTP/1.1 201 Created
Location: /task/f9d4092f-a614-4c58-96f7-8a1e0b564078
```
В блокирующем
```
{"id":"d37442e-54ef-4e65-9255-5fc7618fef","stage":"DoneOk","startTime":1553
267531762,"stopTime":1553267551881,"log":[{"dateTime":"2019-03-22T17:12:12.016",
"message":"Start sleep 2 sec"}]}
```
Список задач
```
curl -X GET http://localhost:8080/task?satge=DoneOk
```
satge может быть: Pending, InProgress, DoneOk, DoneError, Interrupted
