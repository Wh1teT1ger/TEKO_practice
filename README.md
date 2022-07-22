# Практика

## Задание
> 1. Разобраться с тем, что за формат такой JSON
> 2. Выбрать библиотеку работы с JSON на языке программирования, на котором планируете делать
> 3. Изучить протокол работы с платформой ТЕКО:
> - https://docs.teko.io/api-reference/initiator-protocol
> - https://docs.teko.io/api-reference/intro/security
> - https://docs.teko.io/api-reference/merchant-protocol
> 4. Сделать себе репозиторий в гите для проекта
> 5. Разработать приложение для отправки запросов по инициаторскому протоколу
> 6. Разработать приложение для приёма запросов по мерчантскому протоколу

## Отправка запросов по инициаторскому протоколу

Отправка запросов производится консольным приложением
```
java com,practiceApp.initRequestApp.HttpRequest метод [файл.json]
```
###Методы:
- initPayment
- getPaymentsByTag
- getPaymentById
- getPaymentStatus

## Приём запросов по мерчантскому протоколу

Сервер принимает POST запросы

### Методы:
- isPaymentPossible: Проверка возможности проведения платежа.
- resumePayment: Используется только после isPaymentPossible для продолжения платежа. Отправляется полноценный запрос со всей информацией о транзакции.
- cancelPayment: Отмена платежа, для которого ранее производилась проверка с помощью метода isPaymentPossible.
- rollbackPayment: Возврат уже выполненного платежа.

## Примеры запросов серверу:
```
curl -X POST localhost/isPaymentPossible -H "Content-Type: application/json" -d @.../src/main/resources/isPaymentPossible.json
```
```
curl -X POST localhost/resumePayment -H "Content-Type: application/json" -d @.../src/main/resources/resumePayment.json
```
