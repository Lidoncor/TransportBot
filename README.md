# TransportBot
Telegram-бот для получения расписания маршрутов общественного транспорта города Новосибирск.
Бот испрользует API 2GIS для получения информации об остановках рядом с пользователем, и данные сайта ЦУГАЭТ для получения информации об маршрутах.
# Настройки
Для работы бота необходимо поменять следующие переменные в классе Bot.java:
* BOT_TOKEN - Токен, который выдаёт @BotFather при регистрации Вашего бота;
* BOT_NAME  - Имя бота, которое было зарегистрировано у @BotFather;
* GIS_TOKEN - Токен для доступа к API 2GIS, который необходимо запрашивать у 2GIS.
# Пример работы
<img src="media/example.GIF" width="300" height="533" />

