# location-service

Сервис, отвечающий за хранение информации о такси, расписании и причалах

## Общая инструкция

Для успешного запуска приложения необходимо иметь предустановленный и запущенный Docker

### Порядок действий

- Скачать и
  разархивировать [hack-docker.zip](https://github.com/hack-mos/backend-location-service/blob/master/src/main/resources/hack-docker.zip)
- Внутри папки выполнить команду `docker compose up -d`
- Открыть SwaggerUI соответствующих
  сервисов: [Location-Service](http://localhost:8080/q/swagger-ui), [Route Service](http://localhost:8081/q/swagger-ui/)
- **Важно!** Для большинства запросов требуется аутентификация пользователя. Пользователи в Keycloak уже созданы, однако
  необходимо получить `access_token` и передавать его в запрос
- Доступ к Keycloak можно получить по `http://localhost:8543/admin/master/console/`

**Примечания:**

**OpenAPI**:

- [Route Service OpenAPI](https://github.com/hack-mos/backend-route-service/blob/master/src/main/resources/route-service-openapi.yaml)
- [Location Service OpenAPI](https://github.com/hack-mos/backend-location-service/blob/master/src/main/resources/location-service-openapi.yaml)

В Keycloak заведены три пользователя (далее - логин/пароль). Если хочется попасть в Keycloak UI - логин и пароль
`admin`:

1. boss/boss (права Администратора)
2. client/client (права Клиента)
3. driver/driver (права Перевозчика)

**Для получения токена необходимо:**

- Отправить запрос на получение токена:

```curl
curl --request POST \
  --url http://localhost:8543/realms/master/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data grant_type=password \
  --data client_id=hack \
  --data username=boss \
  --data password=boss \
  --data client_secret=A2DR0KSSncEBlUYUykWOJq5VArcCkmaT
```

где `username` - имя пользователя (client / admin / driver), `password` - пароль пользователя (client / admin / driver)

**Пример запроса по получению всех причалов (с пагинацией):**

```curl
curl --request GET \
  --url 'http://localhost:8080/api/v1/place?page=0&size=10' \
  --header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDWDBlZWVVMnRKRERkZjFQOGRMbWd5Q3dhVUlrOGJvTnVRb2dFaDZ3Rl9FIn0.eyJleHAiOjE3MzMwNDc0MDAsImlhdCI6MTczMzAxMTQwMCwianRpIjoiMzU0MTk1ZWQtMzAwNC00MDA5LWE3NzItZDUxMTNlNGY4Y2U1IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4NTQzL3JlYWxtcy9tYXN0ZXIiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYzNkZjU5NTItM2UxMi00NmFiLWI1ZWMtMTNhYzlmYzhiNjJjIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiaGFjayIsInNpZCI6IjNiMDQzYzhhLWIzYjItNGNhOC1iYTFjLTJlMzM5YzQyZjAyYSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiLyoiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbImRlZmF1bHQtcm9sZXMtbWFzdGVyIiwib2ZmbGluZV9hY2Nlc3MiLCJIQUNLX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlFXZXdxZSBRV0V3cWVxd2UiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJib3NzIiwiZ2l2ZW5fbmFtZSI6IlFXZXdxZSIsImZhbWlseV9uYW1lIjoiUVdFd3FlcXdlIn0.XyImNiGbvCKSmZzQEG3ssYoh25pwN6cj1tbqEmJXzoRYsruXyN9bvRZuAUjYstiYdK3oOr290WtIFzTgDBEYm9wNzOf_IUoJG2khsq8CsWXDxwYbt0AGXPa4EY1v0Gy0gQrrDYEKG3VSJIUEDTme3Gw71C-IXkXzYUcSdHNlCJqNVbSeNvBrl5HHuD60ZNSAmhlIhFzDwJI1KDLsZo-v7cGtxFHXu15uLS-pMUnoznT7HYQoRyt2nchTdv-2Qg5aBx4CW7yr5ODITOjTXn-jXUNGcqJ-1-kxJfZmQUDyLu8NfPfXlBr1WVrEg1cgda01HVkS7dv0lKur5WAaMKB6fg' \
  --header 'accept: application/json'
```

**Алгоритм прогнозирования времени в пути и подбор наименее загруженных пришвартовочных мест при подборе маршрута из A и
B:**

```sql
WITH dock_pairs AS (SELECT d1.id AS from_dock,
                           d2.id AS to_dock
                    FROM public.places d1
                           JOIN public.places d2
                                ON d1.id <> d2.id),
     usage_stats AS (SELECT s.dock_id AS dock_id,
                            s.berth_position,
                            COUNT(*)  AS usage_count -- считаем наименее загруженные пришвартовочные места
                     FROM public.schedules s
                     GROUP BY s.dock_id, s.berth_position),
     combined_data AS (SELECT DISTINCT ON (dp.from_dock, dp.to_dock) dp.from_dock,
                                                                     dp.to_dock,
                                                                     s1.ship_name,
                                                                     AVG(EXTRACT(EPOCH FROM (s2.docking_arrival - s1.departure)) / 60) AS avg_travel_time_minutes, -- среднее время между прибытием в точку B и убытием из точки A с учетом времени швартовки корабля
                                                                     us_from.berth_position                                            AS from_berth_position,
                                                                     us_from.usage_count                                               AS from_usage_count,
                                                                     us_to.berth_position                                              AS to_berth_position,
                                                                     us_to.usage_count                                                 AS to_usage_count
                       FROM dock_pairs dp
                              JOIN public.schedules s1
                                   ON s1.dock_id = dp.from_dock
                              JOIN public.schedules s2
                                   ON s2.dock_id = dp.to_dock
                                     AND s1.ship_name =
                                         s2.ship_name -- опираемся на данные по валидным кораблям из расписания
                                     AND s2.departure >
                                         s1.departure -- оставляем только те, которые плывут последовательно (A -> B -> C)
                                     AND DATE(s1.start_date_utc) = DATE(s2.start_date_utc) -- в рамках одного дня
                              LEFT JOIN usage_stats us_from
                                        ON us_from.dock_id = dp.from_dock
                              LEFT JOIN usage_stats us_to
                                        ON us_to.dock_id = dp.to_dock
                       WHERE s1.ship_name IS NOT NULL
                       GROUP BY dp.from_dock, dp.to_dock, s1.ship_name, us_from.berth_position, us_from.usage_count,
                                us_to.berth_position, us_to.usage_count)
SELECT from_dock,
       to_dock,
       ship_name,
       avg_travel_time_minutes,
       from_berth_position,
       from_usage_count,
       to_berth_position,
       to_usage_count
FROM combined_data
WHERE from_dock = :fromDock
  AND to_dock = :toDock
ORDER BY from_dock, to_dock, avg_travel_time_minutes
LIMIT 1;
```

В [hack-dock.zip](https://github.com/hack-mos/backend-location-service/blob/master/src/main/resources/hack-docker.zip)
присутствуют метрики

Они так же доступны
по [Route Service Metrics](http://localhost:8081/q/metrics), [Location Service Metrics](http://localhost:8080/q/metrics)