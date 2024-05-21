![Telegram](https://img.shields.io/badge/Telegram-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white) 	
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) 
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) 
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)

# Lumios

Lumios is a Telegram bot designed to streamline study management. It offers features such as queue management, ratings, statistics, tasks, timetable, and notifications. Lumios is built with pure Java, TelegramBots and Spring Framework.

---

## Table of Contents

- [Features](#features)
- [Development](#development)
- [Installation](#installation)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)
- [Distribution](#distribution)

---

## Features
* Queues (dynamic list of students for some activities)
    * Create queue with custom names
    * Create mixed queue with random order
    * Join and leave queue any time
    * Notify the head of queue about his turn
    * Delete whole queue if required
<br><br>
* Reverence (pretty simple statistics system)
    * Give reputation to other chat mates
    * Take reputation from other chat mates
    * Check the chat reputation statistics
    * Check your own reputation statistics
    * Based on telegram built-in reactions 
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

---

## Development
The project is entirely made using the Java Telegram API Wrapper Library from rubenlagus <a href="https://github.com/rubenlagus/TelegramBots"> TelegramBots</a>, using the Chain of Responsibility pattern which delegates specific updates to different Handlers based on their features and unique content. The general structure also uses the Spring Framework (Spring Data, Spring REST, Spring Security) and Spring Boot launcher particularly as out-of-the-box solutions for many possible problems and vulnerabilities in the code. Additionally, the project can be built using Spring Thin-Jar Plugin, so all the dependencies will be downloaded in Runtime, that can also provide us with the minimized jar which contains only required classes  

---

## Installation
In order to successfully set up the application and launch it in order to somehow enhance the bot, you should specify credentials for your Telegram bot retrieved via BotFather in Telegram in file named ```application.properties``` located in resources folder, and not only. To clone and start the the project you have to:

1. Clone the repository: `git clone https://github.com/ikeepcalm/lumios.git`
2. Navigate to the project directory: `cd lumios`
3. Install dependencies: `gradle build`
4. Configure start-up properties:
```properties
#credentials
telegram.bot.username= #BOT USERNAME
#rest
rest.api.key = #API KEY FOR THE REST API
rest.api.header = #NAME OF THE API KEY HEADER
#database
spring.datasource.url = #URL OF DB CONNECTION
spring.datasource.username = #USERNAME OF THE DB USER
spring.datasource.password = #PASSWORD OF THE DB USER
#other APIs
tenor.api.key = #TENOR API KEY, IF I'D LIKE TO USE IT
```
5. Build the fat-jar using: `gradle clean bootJar`

---

## Contributing

Contributions to Lumios are welcome! To contribute, follow these guidelines:

1. Fork the repository and create a new branch for your feature or bug fix.
2. Make your changes and ensure they adhere to the project's coding standards.
3. Test your changes thoroughly. Maybe, several times.
4. Submit a pull request detailing the changes you've made and the problem they solve.

Optional: Create an Issue on the same project page, where you describe new feature or bug you've encountered as detailed as possible. You may also be asked to provide some more evidence later

---

## License

Lumios is licensed under the [CC BY-NC 4.0 Deed](https://creativecommons.org/licenses/by-nc/4.0/deed.uk).

---

## Contact

For questions, feedback, or inquiries about Lumios, please contact [horokhbohdandmytrovich@gmail.com](mailto:horokhbohdandmytrovich@gmail.com
) or reach directly [here](https://t.me/ikeepcalm).

---

## Distribution
An official online contribution can be found <a href="https://t.me/lumios_bot"> here</a>. <br> Feel free to join and try it out yourself, it's up!
