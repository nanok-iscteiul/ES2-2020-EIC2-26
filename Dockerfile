FROM openjdk:7
COPY C:/Users/nicha/.jenkins/workspace/Projeto-ExecutávelJar/target/ES2-2020-EIC2-26-0.0.1-SNAPSHOT.jar /usr/src/myapp
WORKDIR /usr/src/myapp
CMD ["java", "-jar", "ES2-2020-EIC2-26-0.0.1-SNAPSHOT.jar"]
