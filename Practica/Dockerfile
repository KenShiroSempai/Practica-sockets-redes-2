FROM openjdk:23

COPY . /app
COPY lib/* /app/lib/

WORKDIR /app

RUN javac -cp "/app/lib/*" servidor.java
CMD ["java", "-cp", "/app/lib/*:.", "servidor"]