[//]: # (reverse port)
adb reverse tcp:9191 tcp:9191
adb reverse tcp:8081 tcp:8081
adb reverse tcp:9000 tcp:9000
adb reverse tcp:8080 tcp:8080
adb reverse tcp:9092 tcp:9092
adb reverse tcp:6379 tcp:6379
adb reverse tcp:8761 tcp:8761
adb reverse tcp:9001 tcp:9001
adb reverse tcp:4000 tcp:4000

[//]: # (check port list)
adb reverse --list