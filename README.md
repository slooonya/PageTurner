<a id="readme-top"></a>

<!-- PROJECT LOGO -->
<div align="center">
<img width="1680" height="200" alt="Banner" src="https://github.com/user-attachments/assets/44d048de-6176-4c80-95da-3967b855cfc7" />
</div>    
<br>
  <details>
    <summary>Table of Contents</summary>
    <ol>
      <li>
        <a href="#about-the-project">About The Project</a>
        <ul>
          <li><a href="#built-with">Built With</a></li>
        </ul>
      </li>
      <li>
        <a href="#getting-started">Getting Started</a>
        <ul>
          <li><a href="#prerequisites">Prerequisites</a></li>
          <li><a href="#installation">Installation</a></li>
        </ul>
      </li>
      <li><a href="#contributing">Contributing</a></li>
      <li><a href="#contact">Contact</a></li>
    </ol>
  </details>


<!-- ABOUT THE PROJECT -->
## About The Project
**PageTurner** is a web application designed to help users build and maintain a consistent reading habit through _reading activity tracking_. With this app, you can:

### 📚 Reading
* Create detailed reading logs with book information, reading time, progress, and personal notes
* Edit and delete reading logs
* Search and filter through reading activity
* View reading history and track changes over time
* View reading statistics and progress

### 👤 Accounts
* Customize personal profiles
* Manage account settings
* Email verification during registration
* Secure password reset and account recovery

### 🛡️ Administration
* Manage users and reading logs
* Review and flag logs as violations
* Promote or demote users
* Ban or unban accounts

<img width="1409" height="1916" alt="Screenshots" src="https://github.com/user-attachments/assets/b8e28dcb-c249-4ddb-812e-2bc7bb07c138" />

### Built With

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) ![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white) ![CSS3](https://img.shields.io/badge/css3-%231572B6.svg?style=for-the-badge&logo=css3&logoColor=white) ![JavaScript](https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E) ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- GETTING STARTED -->
## Getting Started
Follow the steps below to get a local copy up and running.

### Prerequisites
Make sure you have the following installed on your system:

1. Java Development Kit (JDK) (version 11 or higher)
   - [Download JDK](https://www.oracle.com/java/technologies/javase-downloads.html)
2. Maven (for dependency management and building the project)
   - [Download Maven](https://maven.apache.org/download.cgi)
3. Git (for cloning the repository)
   - [Download Git](https://git-scm.com/)
4. An IDE (e.g., IntelliJ IDEA, Eclipse, or VS Code) for development.

Verify installations:
```bash
java -version
mvn -version
git --version
```

### Installation

1. Clone the repository:

```bash
git clone https://github.com/slooonya/daily-reading-tracker.git
```

2. Navigate to the project directory:

 ```bash
 cd daily-reading-tracker
 ```

3. Configure the environment variables:
   - Create application.properties file <br>
   - Replace the placeholders below with values for your local environment:

```bash
# Database configuration
spring.datasource.url=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>
spring.datasource.username=<DB_USERNAME>
spring.datasource.password=<DB_PASSWORD>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database=mysql
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.session.jdbc.initialize-schema=always

# Application configuration
spring.application.name=pageturner

# Directory where uploaded files are stored
app.uploads.dir=<UPLOAD_DIRECTORY>

# Public URL used to access uploaded files
app.uploads.host=http://localhost:8080/uploads/

# Administrator registration (Code that will be required when registering an administrator account)
app.admin.registration-code=<ADMIN_REGISTRATION_CODE>

# Email configuration
spring.mail.host=<SMTP_HOST>
spring.mail.port=<SMTP_PORT>
spring.mail.username=<SMTP_USERNAME>
spring.mail.password=<SMTP_PASSWORD>
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.ssl.trust=<SMTP_HOST>
 ```
   
4. Build the project using Maven:
```bash
mvn clean install
```
5. Open your browser and navigate to:
http://localhost:8080

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTRIBUTING -->
## Contributing

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

Sonya's email address: snmmnva@gmail.com

Project Link: [https://github.com/slooonya/PageTurner](https://github.com/slooonya/PageTurner)

<img width="1680" height="200" alt="Footer" src="https://github.com/user-attachments/assets/60ebd63d-7993-4d93-945a-7e168158f68b" />

<p align="right">(<a href="#readme-top">back to top</a>)</p>

