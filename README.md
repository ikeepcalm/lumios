# About
Queue Up Now is pretty simple and open-source telegram bot to create different queues you might need during your education in group, group projects, test and more. Simply create new queue using /queue to use default Queue or /queue {alias} to create the Queue with custom name, join it using Inline Keyboard Buttons in order for it to build list, update the info about Queue using Flush or Exit Buttons, and additionally get reminded about your priority, when it comes to the first one. Pretty simple, pretty useful.

# Development Stack
The project is entirely made using the Java Telegram API Wrapper Library from rubenlagus <a href="https://github.com/rubenlagus/TelegramBots"> TelegramBots</a>, using the Chain of Responsibility pattern which delegates specific updates to different Handlers based on their features and unique content. The general structure also uses the Spring Framework, Spring Boot launcher particulary as out-of-the-box solutions for many possible problems and vulnerabilities in the code. Additionally, the project can be built using Spring Thin-Jar Plugin, so all the dependencies will be downloaded in Runtime, that can also provide us with the minimized jar which contains only Queue logic 

# How to ... ?
In order to successfully set up the application and launch it, you should specify credentials for your Telegram bot retreived via BotFather in Telegram in file named ```thirdparty.properties``` located in resources folder

```properties
#credentials
telegram.bot.token= #BOT TOKEN
telegram.bot.username= #BOT USERNAME
```

# OSI
An official online contribution can be found <a href="https://t.me/queueupnow_bot"> here</a>, but you can also always clone this repo and modify the bot as you wish, just make sure to point to this repo as the original idea
