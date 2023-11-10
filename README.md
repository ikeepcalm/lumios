# Features
Queue Up Now is pretty simple and open-source telegram bot useful mainly for university students, which can offer to you with such features as:
* Queues (dynamic list of students for some activities)
    * Create queue with custom names
    * Join and leave queue any time
    * Notify the head of queue about his turn
    * Delete whole queue if required
<br><br>
* Reverence (pretty simple reputation system)
    * Give reputation to other chat mates
    * Take reputation from other chat mates
    * Check the chat reputation statistics
    * Check your own reputation statistics
    * Increase limit of credits given per day
<br><br>
* Tasks (dynamic list of tasks due on some day)
    * Add task to the task list
    * Edit task in the task list
    * Show the formatted task list
<br><br>
* Timetable (flexible timetable for the chat)
    * Create informative timetable for the week
    * Add time limits of each lesson a day
    * Add URL to the lesson's conferences
    * Auto-notification about the upcoming lesson
    * Web-Interface to set up the timetable

# Development
The project is entirely made using the Java Telegram API Wrapper Library from rubenlagus <a href="https://github.com/rubenlagus/TelegramBots"> TelegramBots</a>, using the Chain of Responsibility pattern which delegates specific updates to different Handlers based on their features and unique content. The general structure also uses the Spring Framework (Spring Data, Spring REST, Spring Security) and Spring Boot launcher particularly as out-of-the-box solutions for many possible problems and vulnerabilities in the code. Additionally, the project can be built using Spring Thin-Jar Plugin, so all the dependencies will be downloaded in Runtime, that can also provide us with the minimized jar which contains only required classes  

# Make it better together
In order to successfully set up the application and launch it in order to somehow enhance the bot, you should specify credentials for your Telegram bot retrieved via BotFather in Telegram in file named ```thirdparty.properties``` located in resources folder

```properties
#credentials
telegram.bot.token= #BOT TOKEN
telegram.bot.username= #BOT USERNAME
rest.api.origin = #CORS WHITELISTED ORIGIN
```

# Official Release
An official online contribution can be found <a href="https://t.me/queueupnow_bot"> here</a>. <br> Feel free to join and try it out yourself, it's up!